package com.auth.service.auth.service;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.model.value.login.CompletedLoginSession;
import com.auth.service.auth.model.value.login.LogoutSessionHint;
import com.auth.service.auth.model.value.login.RefreshSessionResult;
import com.auth.service.auth.model.value.login.TokenPair;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import com.auth.service.auth.model.value.login.command.UsernamePasswordCommand;
import com.auth.service.auth.service.impl.AuthSessionServiceImpl;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.login.LoginService;
import com.auth.service.auth.support.login.RefreshTokenCookieWriter;
import com.auth.service.auth.support.login.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AuthSessionService} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTest {

	@Mock
	private LoginService loginService;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private RefreshTokenCookieWriter refreshTokenCookieWriter;

	@Mock
	private LoginAuditService loginAuditService;

	@InjectMocks
	private AuthSessionServiceImpl authSessionService;

	@Test
	@DisplayName("completeLogin：登录后按 rememberMe 写入 Refresh Cookie")
	void completeLogin_shouldWriteCookieWithRememberMe() {
		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("user");
		command.setPassword("pass");
		command.setRememberMe(true);

		CompletedLoginSession login = new CompletedLoginSession();
		login.setRefreshToken("refresh-token");
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		when(loginService.login(any(BaseLoginCommand.class), same(request))).thenReturn(login);

		UserLoginResponse actual = authSessionService.completeLogin(command, request, response);

		assertEquals("refresh-token", actual.getRefreshToken());
		verify(refreshTokenCookieWriter).writeRefreshToken(response, request, "refresh-token", true);
	}

	@Test
	@DisplayName("completeRefresh：刷新后按会话 rememberMe 重写 Cookie，并下发授权快照")
	void completeRefresh_shouldWriteCookieFromSessionRememberMe() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		Instant expires = Instant.now().plus(1, java.time.temporal.ChronoUnit.HOURS);
		TokenPair tokenPair = TokenPair.builder()
			.accessToken("new-access")
			.refreshToken("new-refresh")
			.accessExpiresAt(expires)
			.build();
		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.username("alice")
			.roles(List.of("ROLE_USER"))
			.permissions(List.of("sys:user:query", "sys:user:update"))
			.permVersion(3L)
			.build();

		when(refreshTokenCookieWriter.readToken(request)).thenReturn("old-refresh");
		when(refreshTokenService.refreshToken("old-refresh", request)).thenReturn(
				RefreshSessionResult.builder().tokenPair(tokenPair).rememberMe(false).authProfile(profile).build());
		when(refreshTokenCookieWriter.resolveReadMeDay()).thenReturn(5L);

		RefreshTokenResponse actual = authSessionService.completeRefresh(request, response);

		assertEquals("new-access", actual.getAccessToken());
		assertEquals("new-refresh", actual.getRefreshToken());
		assertEquals(expires, actual.getExpires());
		assertEquals(5L, actual.getReadMeDay());
		assertEquals(TestConstants.USER_ID, actual.getId());
		assertEquals("alice", actual.getUsername());
		assertEquals(List.of("ROLE_USER"), actual.getRoles());
		assertEquals(List.of("sys:user:query", "sys:user:update"), actual.getPermissions());
		verify(refreshTokenCookieWriter).resolveReadMeDay();
		verify(refreshTokenCookieWriter).writeRefreshToken(response, request, "new-refresh", false);
	}

	@Test
	@DisplayName("completeLogout：撤销 Access/Refresh 会话并始终清除 Cookie")
	void completeLogout_shouldRevokeSessionsAndClearCookie() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
		when(refreshTokenCookieWriter.readToken(request)).thenReturn("refresh-token");
		when(loginService.logout("access-token")).thenReturn(
				Optional.of(LogoutSessionHint.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build()));
		when(refreshTokenService.revokeByRefreshToken("refresh-token")).thenReturn(Optional.empty());

		authSessionService.completeLogout(request, response);

		verify(loginService).logout("access-token");
		verify(refreshTokenService).revokeByRefreshToken("refresh-token");
		verify(loginAuditService).auditLogoutSuccess(same(request), eq(TestConstants.USER_ID), eq(TestConstants.JTI));
		verify(refreshTokenCookieWriter).clearToken(response, request);
	}

	@Test
	@DisplayName("completeLogout：无 Authorization 时仍按 Refresh Cookie 撤销会话")
	void completeLogout_shouldRevokeByRefresh_whenNoAuthorizationHeader() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getHeader("Authorization")).thenReturn(null);
		when(refreshTokenCookieWriter.readToken(request)).thenReturn("refresh-only");
		when(refreshTokenService.revokeByRefreshToken("refresh-only")).thenReturn(
				Optional.of(LogoutSessionHint.builder().userId(TestConstants.USER_ID).jti(TestConstants.JTI).build()));

		authSessionService.completeLogout(request, response);

		verify(loginService, never()).logout(anyString());
		verify(refreshTokenService).revokeByRefreshToken("refresh-only");
		verify(loginAuditService).auditLogoutSuccess(same(request), eq(TestConstants.USER_ID), eq(TestConstants.JTI));
		verify(refreshTokenCookieWriter).clearToken(response, request);
	}

}
