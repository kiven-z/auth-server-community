package com.auth.service.auth.support.login.strategy;

import com.auth.common.data.model.enums.UserStatus;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.exception.AuthBadCredentialsException;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.value.login.AuthenticatedUser;
import com.auth.service.auth.model.value.login.LoginAccount;
import com.auth.service.auth.model.value.login.LoginResult;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 登录策略模板：解析用户 → 锁定检查 → 子类校验凭证 → 构建画像与返回；失败路径经 {@link LoginAuditService} 发布审计。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
public abstract class AbstractLoginStrategyTemplate<T extends BaseLoginCommand> implements LoginStrategy<T> {

	private final AuthProfileRepository authProfileRepository;

	private final LoginAuditService loginAuditService;

	private final LoginFailureRateLimiter loginFailureRateLimiter;

	/**
	 * 当前策略对应的审计事件类型（用于失败时尚未执行 {@link #doAuthenticate} 的场景）。
	 * @return 登录日志类型
	 */
	protected abstract AuthLoginLogType loginLogType();

	@Override
	public LoginResult authenticate(T command, HttpServletRequest request) {
		// 解析登录账户并记录审计日志
		LoginAccount account = resolveAccountOrAuditAndRethrow(command, request);
		Long userId = account.id();

		// 校验用户是否锁定并记录审计日志
		assertNotLockedOrAuditAndRethrow(request, account);

		try {
			// 校验凭证并记录审计日志
			AuthLoginLogType resolvedLoginLogType = doAuthenticate(command, account);
			// 构建认证用户画像
			AuthProfile profile = authProfileRepository.buildByUserId(userId);
			return LoginResult.builder()
				.authenticatedUser(AuthenticatedUser.from(account))
				.authProfile(profile)
				.loginLogType(resolvedLoginLogType)
				.build();
		}
		catch (AuthBadCredentialsException ex) {
			int remaining = loginFailureRateLimiter.recordFailure(userId);
			loginAuditService.auditBadCredentialsWithRemaining(request, loginLogType(), userId, account.username(),
					remaining);
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, remaining);
		}
		catch (AuthBusinessException ex) {
			loginAuditService.auditMappedCredentialFailure(request, loginLogType(), account.id(), account.username(),
					ex);
			throw ex;
		}
	}

	/**
	 * 解析登录账户并记录审计日志。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录账户
	 */
	private LoginAccount resolveAccountOrAuditAndRethrow(T command, HttpServletRequest request) {
		try {
			return resolveAccount(command);
		}
		catch (AuthBusinessException ex) {
			if (ex.getResultCode() == AuthResultCode.USERNAME_OR_PASSWORD_ERROR) {
				loginAuditService.auditUserUnknownAfterResolve(request, loginLogType(),
						loginAuditService.resolvePrincipal(command), ex.getMessageKey());
			}
			throw ex;
		}
	}

	/**
	 * 校验用户是否锁定并记录审计日志。
	 * @param request HTTP 请求
	 * @param account 登录账户
	 */
	private void assertNotLockedOrAuditAndRethrow(HttpServletRequest request, LoginAccount account) {
		Long userId = account.id();
		try {
			UserStatus status = UserStatus.ofNullable(account.status());
			if (status == null || status == UserStatus.DISABLED) {
				loginAuditService.auditAccountDisabled(request, loginLogType(), userId, account.username(),
						AuthResultCode.USER_DISABLED.getMessageKey());
				throw new AuthBusinessException(AuthResultCode.USER_DISABLED);
			}
			if (status == UserStatus.LOCKED) {
				loginAuditService.auditAccountLockedByStatus(request, loginLogType(), userId, account.username());
				throw new AuthBusinessException(AuthResultCode.USER_LOCKED);
			}

			loginFailureRateLimiter.assertNotLocked(userId);
		}
		catch (AuthBusinessException ex) {
			loginAuditService.auditMappedLockFailure(request, loginLogType(), account.id(), account.username(), ex);
			throw ex;
		}
	}

	/**
	 * 解析认证命令对应的登录账户。
	 * @param command 登录命令
	 * @return 登录账户
	 */
	protected abstract LoginAccount resolveAccount(T command);

	/**
	 * 校验凭证；密码不匹配时抛出 {@link AuthBadCredentialsException}。
	 * @param command 登录命令
	 * @param account 已解析账户
	 * @return 登录类型（与审计类型一致）
	 */
	protected abstract AuthLoginLogType doAuthenticate(T command, LoginAccount account);

}
