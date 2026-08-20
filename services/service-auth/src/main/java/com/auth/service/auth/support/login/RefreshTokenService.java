package com.auth.service.auth.support.login;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.value.login.LogoutSessionHint;
import com.auth.service.auth.model.value.login.RefreshSessionResult;
import com.auth.service.auth.model.value.login.TokenPair;
import com.auth.service.auth.support.redis.AuthProfileRedisCache;
import com.auth.service.auth.support.session.RefreshRotateCommand;
import com.auth.service.auth.support.session.RefreshRotateOutcome;
import com.auth.service.auth.support.session.RefreshRotateResult;
import com.auth.service.auth.support.session.UserSessionRedisStore;
import com.auth.service.auth.support.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * 刷新令牌服务
 *
 * @author Bunny
 */
@Slf4j
@Service
public class RefreshTokenService {

	/**
	 * 上一轮 refresh 在短窗内可幂等复用（多标签并发）
	 */
	private static final long REFRESH_REUSE_GRACE_MS = 30_000L;

	private final AuthProfileRedisCache authProfileRedisCache;

	private final UserSessionRedisStore userSessionRedisStore;

	private final TokenService tokenService;

	private final JwtProperties jwtProperties;

	private final LoginAuditService loginAuditService;

	public RefreshTokenService(AuthProfileRedisCache authProfileRedisCache, UserSessionRedisStore userSessionRedisStore,
			TokenService tokenService, JwtProperties jwtProperties, LoginAuditService loginAuditService) {
		this.authProfileRedisCache = authProfileRedisCache;
		this.userSessionRedisStore = userSessionRedisStore;
		this.tokenService = tokenService;
		this.jwtProperties = jwtProperties;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 刷新令牌
	 * @param refreshToken 刷新令牌
	 * @param request 当前 HTTP 请求（审计 IP/UA）；允许为 null
	 * @return 刷新令牌结果（含 rememberMe，供 Cookie 策略使用）
	 */
	public RefreshSessionResult refreshToken(String refreshToken, HttpServletRequest request) {
		try {
			if (CharSequenceUtil.isBlank(refreshToken)) {
				throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MISSING);
			}

			JwtUserToken refreshUserToken = tokenService.parseRefreshToken(refreshToken);
			Long refreshUserId = refreshUserToken.getUserId();
			String refreshJti = refreshUserToken.getJti();

			UserSessionIndex sessionIndex = userSessionRedisStore.loadUserSessionIndex(refreshJti)
				.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
			Long sessionUserId = sessionIndex.getUserId();
			if (!Objects.equals(refreshUserId, sessionUserId)) {
				long indexUserId = Objects.requireNonNullElse(sessionUserId, refreshUserId);
				userSessionRedisStore.terminateSession(indexUserId, refreshJti);
				throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED);
			}

			AuthProfile profile = authProfileRedisCache.loadCachedProfile(refreshUserId)
				.orElseThrow(() -> new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED));
			Long permVersion = Objects.requireNonNull(profile.getPermVersion(), "permVersion must not be null");

			TokenPair candidate = tokenService.buildTokenPair(refreshUserId, refreshJti, permVersion);
			long refreshExpiredSeconds = jwtProperties.getRefreshExpired();
			RefreshRotateResult rotateResult = userSessionRedisStore.rotateRefresh(RefreshRotateCommand.builder()
				.jti(refreshJti)
				.requestRefreshHash(MD5.create().digestHex16(refreshToken))
				.newRefreshHash(MD5.create().digestHex16(candidate.refreshToken()))
				.newRefreshExpiresAtMs(System.currentTimeMillis() + refreshExpiredSeconds * 1000L)
				.ttl(Duration.ofSeconds(refreshExpiredSeconds))
				.graceMs(REFRESH_REUSE_GRACE_MS)
				.accessToken(candidate.accessToken())
				.refreshToken(candidate.refreshToken())
				.accessExpiresAtMs(candidate.accessExpiresAt().toEpochMilli())
				.build());

			RefreshRotateOutcome outcome = rotateResult.outcome();
			TokenPair tokenPair;
			if (outcome == RefreshRotateOutcome.ROTATED) {
				tokenPair = candidate;
			}
			else if (outcome == RefreshRotateOutcome.REUSED) {
				tokenPair = TokenPair.builder()
					.accessToken(rotateResult.accessToken())
					.refreshToken(rotateResult.refreshToken())
					.accessExpiresAt(rotateResult.accessExpiresAt())
					.build();
			}
			else {
				if (outcome == RefreshRotateOutcome.MISMATCH) {
					userSessionRedisStore.terminateSession(refreshUserId, refreshJti);
				}
				throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_EXPIRED);
			}

			boolean rememberMe = sessionIndex.getRememberMe() != null && sessionIndex.getRememberMe();
			loginAuditService.auditRefreshTokenSuccess(request, refreshUserId, refreshJti);
			return RefreshSessionResult.builder()
				.tokenPair(tokenPair)
				.rememberMe(rememberMe)
				.authProfile(profile)
				.build();
		}
		catch (AuthBusinessException ex) {
			Long userId = tryResolveUserIdFromRefreshToken(refreshToken);
			loginAuditService.auditRefreshTokenFailure(request, userId, ex);
			throw ex;
		}
	}

	/**
	 * 尝试从刷新令牌中解析用户ID
	 * @param refreshToken 刷新令牌
	 * @return 用户ID
	 */
	private Long tryResolveUserIdFromRefreshToken(String refreshToken) {
		if (CharSequenceUtil.isBlank(refreshToken)) {
			return null;
		}
		try {
			JwtUserToken tokenResult = tokenService.parseRefreshToken(refreshToken);
			return tokenResult.getUserId();
		}
		catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * 按刷新令牌撤销服务端会话（登出等场景；令牌无效时静默跳过）
	 * @param refreshToken 刷新令牌
	 */
	public Optional<LogoutSessionHint> revokeByRefreshToken(String refreshToken) {
		if (CharSequenceUtil.isBlank(refreshToken)) {
			return Optional.empty();
		}
		try {
			JwtUserToken userToken = tokenService.parseRefreshToken(refreshToken);

			String jti = userToken.getJti();
			Long userId = userToken.getUserId();
			userSessionRedisStore.terminateSession(userId, jti);

			LogoutSessionHint logoutSessionHint = LogoutSessionHint.builder().userId(userId).jti(jti).build();
			return Optional.of(logoutSessionHint);
		}
		catch (RuntimeException ex) {
			log.error("Failed to revoke session by refresh token", ex);
			throw ex;
		}
	}

}
