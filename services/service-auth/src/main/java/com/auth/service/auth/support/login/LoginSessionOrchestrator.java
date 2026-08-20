package com.auth.service.auth.support.login;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.value.login.AuthenticatedUser;
import com.auth.service.auth.model.value.login.CompletedLoginSession;
import com.auth.service.auth.model.value.login.LoginResult;
import com.auth.service.auth.model.value.login.TokenPair;
import com.auth.service.auth.support.redis.AuthProfileRedisCache;
import com.auth.service.auth.support.session.SessionLimitGuard;
import com.auth.service.auth.support.session.UserSessionIndexFactory;
import com.auth.service.auth.support.session.UserSessionRedisStore;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import com.auth.service.auth.support.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 登录会话签发编排：凭证校验通过后，统一完成会话上限检查、画像缓存、令牌签发、Redis 会话建立与成功审计。
 * <p>
 * 供密码/邮箱/短信登录及后续第三方登录、MFA 通过后复用，避免在 LoginApplicationService 中重复编排。
 * </p>
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class LoginSessionOrchestrator {

	private final SessionLimitGuard sessionLimitGuard;

	private final UserSessionIndexFactory userSessionIndexFactory;

	private final UserSessionRedisStore userSessionRedisStore;

	private final AuthProfileRedisCache authProfileRedisCache;

	private final TokenService tokenService;

	private final LoginAuditService loginAuditService;

	private final RefreshTokenCookieWriter refreshTokenCookieWriter;

	private final LoginFailureRateLimiter loginFailureRateLimiter;

	/**
	 * 在凭证已校验通过后签发令牌并建立服务端会话。
	 * @param loginResult 策略认证结果（含画像与审计类型）
	 * @param rememberMe 是否记住我
	 * @param request HTTP 请求（会话索引与审计）
	 * @return 已填充访问/刷新令牌及 readMeDay 的登录会话读模型
	 */
	public CompletedLoginSession issueSession(LoginResult loginResult, boolean rememberMe, HttpServletRequest request) {
		AuthProfile profile = loginResult.authProfile();
		Long userId = profile.getUserId();
		AuthenticatedUser user = loginResult.authenticatedUser();
		String username = user.username();
		AuthLoginLogType loginLogType = loginResult.loginLogType();

		CompletedLoginSession session = new CompletedLoginSession();
		session.setId(user.id());
		session.setUsername(user.username());
		session.setRoles(profile.getRoles());
		session.setPermissions(profile.getPermissions());

		String jti = null;
		boolean profileCached = false;
		boolean sessionCreated = false;
		try {
			sessionLimitGuard.assertSessionLimit(userId);

			jti = UUID.randomUUID().toString();
			long permVersion = profile.getPermVersion();

			authProfileRedisCache.cacheProfiles(List.of(profile));
			profileCached = true;

			TokenPair tokenPair = tokenService.buildTokenPair(userId, jti, permVersion);
			session.setAccessToken(tokenPair.accessToken());
			session.setRefreshToken(tokenPair.refreshToken());
			session.setExpires(tokenPair.accessExpiresAt());

			UserSessionIndex sessionIndex = userSessionIndexFactory.buildSessionIndex(request, userId, jti,
					session.getRefreshToken(), rememberMe);
			userSessionRedisStore.registerSession(sessionIndex);
			sessionCreated = true;

			session.setReadMeDay(refreshTokenCookieWriter.resolveReadMeDay());

			loginFailureRateLimiter.recordSuccess(userId);
			loginAuditService.auditLoginSuccess(request, loginLogType, userId, username, jti);
			return session;
		}
		catch (AuthBusinessException ex) {
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			loginAuditService.auditOrchestrationFailure(request, loginLogType, userId, username, ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			rollbackLoginArtifacts(userId, jti, profileCached, sessionCreated);
			log.error("Login session issuance failed for userId={}", userId, ex);
			throw ex;
		}
	}

	/**
	 * 会话签发失败时回滚已写入的 Redis 状态，避免孤儿画像或会话
	 * @param userId 用户ID
	 * @param jti 令牌ID
	 * @param profileCached 画像是否已缓存
	 * @param sessionCreated 会话是否已创建
	 */
	private void rollbackLoginArtifacts(Long userId, String jti, boolean profileCached, boolean sessionCreated) {
		if (sessionCreated && jti != null) {
			userSessionRedisStore.terminateSession(userId, jti);
		}
		if (profileCached) {
			authProfileRedisCache.evictProfile(userId);
		}
	}

}
