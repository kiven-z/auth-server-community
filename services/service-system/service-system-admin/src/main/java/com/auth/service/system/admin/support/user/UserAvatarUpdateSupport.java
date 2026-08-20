package com.auth.service.system.admin.support.user;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.module.file.api.policy.FileBizType;
import com.auth.module.file.api.port.OwnedFileOperations;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executor;

/**
 * 用户头像写入与旧图清理
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserAvatarUpdateSupport {

	private final SysUserMapper sysUserMapper;

	private final OwnedFileOperations ownedFileOperations;

	private final Executor adminAsyncExecutor;

	public UserAvatarUpdateSupport(SysUserMapper sysUserMapper, OwnedFileOperations ownedFileOperations,
			@Qualifier("adminAsyncExecutor") Executor adminAsyncExecutor) {
		this.sysUserMapper = sysUserMapper;
		this.ownedFileOperations = ownedFileOperations;
		this.adminAsyncExecutor = adminAsyncExecutor;
	}

	/**
	 * 写入头像并在事务提交后异步清理旧图
	 * @param targetUser 目标用户
	 * @param avatar 新头像 URL
	 * @param fileOwnerUserId 文件归属校验用的用户 ID
	 */
	public void applyAvatarUpdate(UserEntity targetUser, String avatar, Long fileOwnerUserId) {
		String oldAvatar = targetUser.getAvatar();
		if (CharSequenceUtil.isBlank(avatar) || CharSequenceUtil.equals(oldAvatar, avatar)) {
			return;
		}

		try {
			OwnedFileAssertByUrlRequest assertRequest = new OwnedFileAssertByUrlRequest();
			assertRequest.setUrl(avatar);
			assertRequest.setOwnerUserId(fileOwnerUserId);
			assertRequest.setBizType(FileBizType.AVATAR.getCode());
			ownedFileOperations.assertOwnedFileUrl(assertRequest);
		}
		catch (RuntimeException ex) {
			throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, ex.getMessage());
		}

		targetUser.setAvatar(avatar);
		sysUserMapper.updateById(targetUser);

		cleanupOldAvatarAfterCommit(targetUser.getId(), oldAvatar);
	}

	/**
	 * 事务提交后再异步清理旧头像
	 * @param ownerUserId 文件归属用户 ID
	 * @param oldAvatarUrl 待清理的旧头像 URL
	 */
	private void cleanupOldAvatarAfterCommit(Long ownerUserId, String oldAvatarUrl) {
		if (ownerUserId == null || CharSequenceUtil.isBlank(oldAvatarUrl)) {
			return;
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			submitCleanupOldAvatar(ownerUserId, oldAvatarUrl);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				submitCleanupOldAvatar(ownerUserId, oldAvatarUrl);
			}
		});
	}

	/**
	 * 将旧头像清理提交到执行器
	 * @param ownerUserId 文件归属用户 ID
	 * @param oldAvatarUrl 待清理的旧头像 URL
	 */
	private void submitCleanupOldAvatar(Long ownerUserId, String oldAvatarUrl) {
		adminAsyncExecutor.execute(() -> {
			try {
				OwnedFileDeleteByUrlRequest deleteRequest = new OwnedFileDeleteByUrlRequest();
				deleteRequest.setUrl(oldAvatarUrl);
				deleteRequest.setOwnerUserId(ownerUserId);
				deleteRequest.setDeleteSource(FileDeleteSource.SYSTEM_ACTION.getCode());
				deleteRequest.setBizType(FileBizType.AVATAR.getCode());
				ownedFileOperations.tryDeleteOwnedByUrl(deleteRequest);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to cleanup old avatar: ownerUserId={}, url={}, error={}", ownerUserId, oldAvatarUrl,
						ex.getMessage());
			}
		});
	}

}
