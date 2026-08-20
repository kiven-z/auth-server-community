package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.convert.admin.user.SysUserConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserAvatarUpdateForm;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.admin.support.user.UserAvatarUpdateSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, UserEntity> implements SysUserService {

	/**
	 * 新建用户默认权限版本号
	 */
	private static final Long DEFAULT_PERM_VERSION = 0L;

	private final GrantTableMapper grantTableMapper;

	private final UserReferenceChecker userReferenceChecker;

	private final UserAvatarUpdateSupport userAvatarUpdateSupport;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private final UserSessionRevocationTrigger userSessionRevocationTrigger;

	private final PasswordEncoder passwordEncoder;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createBatchFromImport(List<SysUserForm> forms) {
		if (CollUtil.isEmpty(forms)) {
			return;
		}

		List<String> usernames = forms.stream().map(SysUserForm::getUsername).toList();
		List<String> emails = forms.stream().map(SysUserForm::getEmail).toList();
		List<String> phones = forms.stream().map(SysUserForm::getPhone).toList();
		List<String> employeeNos = forms.stream().map(SysUserForm::getEmployeeNo).toList();
		userReferenceChecker.requireAbsentUserBusinessKeys(usernames, emails, phones, employeeNos, null);

		List<UserEntity> entities = forms.stream().map(form -> {
			UserEntity entity = SysUserConverter.INSTANCE.toEntity(form);
			entity.setPassword(passwordEncoder.encode(form.getInitialPassword()));
			if (form.getBirthday() != null) {
				entity.setAge(Period.between(form.getBirthday(), LocalDate.now()).getYears());
			}
			entity.setPermVersion(DEFAULT_PERM_VERSION);
			return entity;
		}).toList();

		super.saveBatch(entities, BatchSizes.SIZE_500);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysUserForm form) {
		Long id = form.getId();
		UserEntity existing = userReferenceChecker.getExistingActive(id);
		List<Long> userIds = List.of(id);
		String username = form.getUsername();
		String email = form.getEmail();
		String phone = form.getPhone();
		String employeeNo = form.getEmployeeNo();
		List<String> employeeNos = CharSequenceUtil.isNotBlank(employeeNo) ? List.of(employeeNo) : List.of();

		userReferenceChecker.requireOperable(userIds);
		userReferenceChecker.requireAbsentUserBusinessKeys(List.of(username), List.of(email), List.of(phone),
				employeeNos, id);

		boolean sensitiveChanged = FieldChangeSupport.anyChanged(
				FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus()),
				FieldChangeSupport.codeChanged(existing.getUsername(), username),
				FieldChangeSupport.codeChanged(existing.getEmail(), email),
				FieldChangeSupport.codeChanged(existing.getPhone(), phone));
		if (sensitiveChanged) {
			userAuthorizationInvalidationTrigger.submitByUserIds(userIds, "update");
			userSessionRevocationTrigger.revokeAllSessionsAfterCommit(userIds);
		}

		SysUserConverter.INSTANCE.applyUpdateForm(form, existing);
		if (form.getBirthday() != null) {
			existing.setAge(Period.between(form.getBirthday(), LocalDate.now()).getYears());
		}

		super.updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteByIds(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<Long> idsToDelete = ids.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(idsToDelete)) {
			return;
		}

		userReferenceChecker.requireOperable(idsToDelete);
		userAuthorizationInvalidationTrigger.submitByUserIds(idsToDelete, "delete");
		grantTableMapper.deleteBySubjectIds(GrantTableSubjectType.USER.name(), idsToDelete);
		removeByIds(idsToDelete);
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(idsToDelete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(SysUserBatchStatusForm form) {
		Integer status = form.getStatus();
		if (UserStatus.of(status) == null) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_STATUS_INVALID);
		}

		List<Long> idsToUpdate = form.getIds().stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(idsToUpdate)) {
			return;
		}

		userReferenceChecker.requireOperable(idsToUpdate);
		List<UserEntity> updates = idsToUpdate.stream().map(userId -> {
			UserEntity entity = new UserEntity();
			entity.setId(userId);
			entity.setStatus(status);
			return entity;
		}).toList();
		super.updateBatchById(updates);

		userAuthorizationInvalidationTrigger.submitByUserIds(idsToUpdate, "update");
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(idsToUpdate);
	}

	@Override
	public void updateAvatar(@Valid SysUserAvatarUpdateForm form) {
		Long userId = form.getUserId();
		userReferenceChecker.requireOperable(List.of(userId));

		String avatar = form.getAvatar();
		Long operatorUserId = SecurityUserUtils.getUserId();
		UserEntity userEntity = userReferenceChecker.getExistingActive(userId);
		userAvatarUpdateSupport.applyAvatarUpdate(userEntity, avatar, operatorUserId);
	}

}
