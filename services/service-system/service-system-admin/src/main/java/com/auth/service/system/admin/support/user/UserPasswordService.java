package com.auth.service.system.admin.support.user;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserAdminResetPasswordForm;
import com.auth.service.system.admin.model.form.user.SysUserChangePasswordForm;
import com.auth.service.system.admin.service.admin.LogUserPasswordHistoryService;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 用户密码变更（自改与管理员重置）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class UserPasswordService {

	private final SysUserMapper sysUserMapper;

	private final LogUserPasswordHistoryService logUserPasswordHistoryService;

	private final UserSessionRevocationTrigger userSessionRevocationTrigger;

	private final UserReferenceChecker userReferenceChecker;

	private final PasswordEncoder passwordEncoder;

	/**
	 * 校验确认密码、更新密码、记录历史并踢出全部会话
	 * @param user 目标用户
	 * @param newPassword 新密码明文
	 * @param confirmPassword 确认密码明文
	 * @param changeIp 客户端 IP
	 */
	private void applyPasswordChange(UserEntity user, String newPassword, String confirmPassword, String changeIp) {
		if (!Objects.equals(newPassword, confirmPassword)) {
			throw new SystemBusinessException(SystemAdminResultCode.PASSWORD_CONFIRM_MISMATCH);
		}

		String encodedPassword = passwordEncoder.encode(newPassword);
		user.setPassword(encodedPassword);
		sysUserMapper.updateById(user);

		logUserPasswordHistoryService.recordChange(user.getId(), encodedPassword, changeIp);
		userSessionRevocationTrigger.revokeAllSessionsAfterCommit(List.of(user.getId()));
	}

	/**
	 * 当前登录用户修改自己的密码
	 * @param form 旧密码与新密码
	 * @param changeIp 客户端 IP
	 */
	public void changeOwnPassword(SysUserChangePasswordForm form, String changeIp) {
		Long userId = SecurityUserUtils.getUserId();
		UserEntity existing = userReferenceChecker.getExistingActive(userId);

		if (!passwordEncoder.matches(form.getOldPassword(), existing.getPassword())) {
			throw new SystemBusinessException(SystemAdminResultCode.OLD_PASSWORD_INCORRECT);
		}

		applyPasswordChange(existing, form.getNewPassword(), form.getConfirmPassword(), changeIp);
	}

	/**
	 * 管理员重置用户密码
	 * @param userId 目标用户主键
	 * @param form 新密码
	 * @param changeIp 客户端 IP
	 */
	public void resetPasswordByAdmin(Long userId, SysUserAdminResetPasswordForm form, String changeIp) {
		UserEntity existing = userReferenceChecker.getExistingActive(userId);

		userReferenceChecker.requireOperable(List.of(userId));
		applyPasswordChange(existing, form.getNewPassword(), form.getConfirmPassword(), changeIp);
	}

}
