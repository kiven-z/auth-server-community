package com.auth.service.auth.service.impl;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.convert.LoginSessionConverter;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.model.value.login.CompletedLoginSession;
import com.auth.service.auth.model.value.login.LogoutSessionHint;
import com.auth.service.auth.model.value.login.RefreshSessionResult;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import com.auth.service.auth.service.AuthSessionService;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.login.LoginService;
import com.auth.service.auth.support.login.RefreshTokenCookieWriter;
import com.auth.service.auth.support.login.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.auth.common.jwt.provider.AbstractJwtTokenProvider.BEARER_PREFIX;

/**
 * HTTP 会话编排实现：登录/刷新/登出与 Refresh Cookie 写入
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthSessionServiceImpl implements AuthSessionService {

	private final LoginService loginService;

	private final RefreshTokenService refreshTokenService;

	private final RefreshTokenCookieWriter refreshTokenCookieWriter;

	private final LoginAuditService loginAuditService;

	/**
	 * 完成登录：签发令牌、写入 Redis 会话、下发 Refresh Cookie
	 * @param command 登录命令（含 rememberMe）
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 登录 API 响应
	 */
	@Override
	public UserLoginResponse completeLogin(BaseLoginCommand command, HttpServletRequest request,
			HttpServletResponse response) {
		CompletedLoginSession session = loginService.login(command, request);
		String refreshToken = session.getRefreshToken();
		boolean rememberMe = command.getRememberMe() != null && command.getRememberMe();

		refreshTokenCookieWriter.writeRefreshToken(response, request, refreshToken, rememberMe);
		return LoginSessionConverter.INSTANCE.toUserLoginResponse(session);
	}

	/**
	 * 完成刷新：旋转令牌、按会话 rememberMe 重写 Refresh Cookie
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 刷新 API 响应
	 */
	@Override
	public RefreshTokenResponse completeRefresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = refreshTokenCookieWriter.readToken(request);
		RefreshSessionResult result = refreshTokenService.refreshToken(refreshToken, request);

		RefreshTokenResponse refreshResponse = new RefreshTokenResponse();
		refreshResponse.setAccessToken(result.tokenPair().accessToken());
		refreshResponse.setRefreshToken(result.tokenPair().refreshToken());
		refreshResponse.setExpires(result.tokenPair().accessExpiresAt());

		AuthProfile profile = Objects.requireNonNull(result.authProfile(), "authProfile must not be null");
		refreshResponse.setId(profile.getUserId());
		refreshResponse.setUsername(profile.getUsername());
		refreshResponse.setRoles(List.copyOf(profile.getRoles()));
		refreshResponse.setPermissions(List.copyOf(profile.getPermissions()));
		refreshResponse.setReadMeDay(refreshTokenCookieWriter.resolveReadMeDay());

		refreshTokenCookieWriter.writeRefreshToken(response, request, result.tokenPair().refreshToken(),
				result.rememberMe());
		return refreshResponse;
	}

	/**
	 * 完成登出：撤销服务端会话、写入单条登出审计并清除 Refresh Cookie
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 */
	@Override
	public void completeLogout(HttpServletRequest request, HttpServletResponse response) {
		try {
			Optional<LogoutSessionHint> auditHint = Optional.empty();
			String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

			if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
				String token = authHeader.substring(BEARER_PREFIX.length());
				auditHint = loginService.logout(token);
			}

			String refreshToken = refreshTokenCookieWriter.readToken(request);
			Optional<LogoutSessionHint> refreshHint = refreshTokenService.revokeByRefreshToken(refreshToken);
			auditHint = auditHint.or(() -> refreshHint);
			auditHint.ifPresent(hint -> loginAuditService.auditLogoutSuccess(request, hint.userId(), hint.jti()));
		}
		finally {
			refreshTokenCookieWriter.clearToken(response, request);
		}
	}

}
