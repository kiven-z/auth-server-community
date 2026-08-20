package com.auth.service.auth.support.login;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link LoginSessionOrchestrator} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginSessionOrchestratorTest {

	@Mock
	private SessionLimitGuard sessionLimitGuard;

	@Mock
	private UserSessionIndexFactory userSessionIndexFactory;

	@Mock
	private UserSessionRedisStore userSessionRedisStore;

	@Mock
	private AuthProfileRedisCache authProfileRedisCache;

	@Mock
	private TokenService tokenService;

	@Mock
	private LoginFailureRateLimiter loginFailureRateLimiter;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private RefreshTokenCookieWriter refreshTokenCookieWriter;

	@Mock
	private HttpServletRequest httpServletRequest;

	private static LoginResult toLoginResult(AuthProfile profile) {
		AuthenticatedUser user = AuthenticatedUser.builder().id(TestConstants.USER_ID).username("user").build();
		return LoginResult.builder()
			.authenticatedUser(user)
			.authProfile(profile)
			.loginLogType(AuthLoginLogType.LOGIN_PASSWORD)
			.build();
	}

	private LoginSessionOrchestrator newOrchestrator() {
		return new LoginSessionOrchestrator(sessionLimitGuard, userSessionIndexFactory, userSessionRedisStore,
				authProfileRedisCache, tokenService, loginAuditService, refreshTokenCookieWriter,
				loginFailureRateLimiter);
	}

	@Test
	@DisplayName("会话签发：按编排顺序执行并返回已填充令牌的读模型")
	void issueSession_shouldOrchestrateInOrder_whenInputsValid() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);
		when(profile.getRoles()).thenReturn(List.of("COMMON"));
		when(profile.getPermissions()).thenReturn(List.of("system:user:list"));

		LoginResult loginResult = toLoginResult(profile);

		UserSessionIndex sessionIndex = new UserSessionIndex();
		sessionIndex.setUserId(TestConstants.USER_ID);
		sessionIndex.setSessionId(TestConstants.JTI);

		when(userSessionIndexFactory.buildSessionIndex(any(), anyLong(), anyString(), anyString(), anyBoolean()))
			.thenReturn(sessionIndex);
		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(TokenPair.builder()
			.accessToken(TestConstants.ACCESS_TOKEN)
			.refreshToken(TestConstants.REFRESH_TOKEN)
			.accessExpiresAt(Instant.now().plus(1, java.time.temporal.ChronoUnit.MINUTES))
			.build());
		when(refreshTokenCookieWriter.resolveReadMeDay()).thenReturn(7L);

		CompletedLoginSession actual = newOrchestrator().issueSession(loginResult, false, httpServletRequest);

		assertEquals(TestConstants.ACCESS_TOKEN, actual.getAccessToken());
		assertEquals(TestConstants.REFRESH_TOKEN, actual.getRefreshToken());
		assertNotNull(actual.getExpires());
		assertEquals(7L, actual.getReadMeDay());
		assertEquals(TestConstants.USER_ID, actual.getId());
		assertEquals("user", actual.getUsername());
		assertEquals(List.of("COMMON"), actual.getRoles());
		assertEquals(List.of("system:user:list"), actual.getPermissions());

		InOrder inOrder = inOrder(sessionLimitGuard, userSessionIndexFactory, userSessionRedisStore,
				authProfileRedisCache, tokenService, refreshTokenCookieWriter, loginFailureRateLimiter,
				loginAuditService);
		inOrder.verify(sessionLimitGuard).assertSessionLimit(TestConstants.USER_ID);
		inOrder.verify(authProfileRedisCache).cacheProfiles(List.of(profile));
		inOrder.verify(tokenService).buildTokenPair(eq(TestConstants.USER_ID), anyString(), eq(1L));
		inOrder.verify(userSessionIndexFactory)
			.buildSessionIndex(same(httpServletRequest), eq(TestConstants.USER_ID), anyString(),
					eq(TestConstants.REFRESH_TOKEN), eq(false));
		inOrder.verify(userSessionRedisStore).registerSession(sessionIndex);
		inOrder.verify(refreshTokenCookieWriter).resolveReadMeDay();
		inOrder.verify(loginFailureRateLimiter).recordSuccess(TestConstants.USER_ID);
		inOrder.verify(loginAuditService)
			.auditLoginSuccess(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID),
					eq("user"), anyString());
	}

	@Test
	@DisplayName("会话签发：rememberMe=true 时写入会话索引")
	void issueSession_shouldPassRememberMe_whenRememberMeTrue() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);
		when(profile.getRoles()).thenReturn(List.of());
		when(profile.getPermissions()).thenReturn(List.of());

		LoginResult loginResult = toLoginResult(profile);

		when(userSessionIndexFactory.buildSessionIndex(any(), anyLong(), anyString(), anyString(), eq(true)))
			.thenReturn(new UserSessionIndex());
		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong())).thenReturn(TokenPair.builder()
			.accessToken(TestConstants.ACCESS_TOKEN)
			.refreshToken(TestConstants.REFRESH_TOKEN)
			.accessExpiresAt(Instant.now().plus(1, java.time.temporal.ChronoUnit.MINUTES))
			.build());

		newOrchestrator().issueSession(loginResult, true, httpServletRequest);

		verify(userSessionIndexFactory).buildSessionIndex(same(httpServletRequest), eq(TestConstants.USER_ID),
				anyString(), anyString(), eq(true));
	}

	@Test
	@DisplayName("会话签发：令牌构建失败时回滚画像并写编排失败审计")
	void issueSession_shouldRollbackArtifacts_whenTokenBuildFails() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(TestConstants.USER_ID);
		when(profile.getPermVersion()).thenReturn(1L);

		LoginResult loginResult = toLoginResult(profile);

		when(tokenService.buildTokenPair(anyLong(), anyString(), anyLong()))
			.thenThrow(new AuthBusinessException(AuthResultCode.SERVER_ERROR));

		LoginSessionOrchestrator orchestrator = newOrchestrator();
		assertThrows(AuthBusinessException.class,
				() -> orchestrator.issueSession(loginResult, false, httpServletRequest));

		verify(authProfileRedisCache).cacheProfiles(List.of(profile));
		verify(authProfileRedisCache).evictProfile(TestConstants.USER_ID);
		verify(userSessionRedisStore, never()).registerSession(any());
		verify(loginAuditService).auditOrchestrationFailure(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID), eq("user"),
				any(AuthBusinessException.class));
	}

}
