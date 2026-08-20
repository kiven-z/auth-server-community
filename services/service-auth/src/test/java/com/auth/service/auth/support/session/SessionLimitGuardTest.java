package com.auth.service.auth.support.session;

import com.auth.module.security.autoconfigure.config.user.SessionLimitStrategy;
import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * {@link SessionLimitGuard#assertSessionLimit(Long)} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionLimitGuardTest {

	@Mock
	private UserConfigProperties userConfigProperties;

	@Mock
	private UserSessionRedisStore userSessionRedisStore;

	private SessionLimitGuard sessionLimitGuard;

	@BeforeEach
	void setUp() {
		sessionLimitGuard = new SessionLimitGuard(userConfigProperties, userSessionRedisStore);
	}

	@Test
	@DisplayName("会话上限：maxSessionCount 为空或<=0 时直接放行")
	void assertSessionLimit_shouldReturn_whenMaxSessionCountNullOrLe0() {
		when(userConfigProperties.getMaxSessionCount()).thenReturn(null);

		sessionLimitGuard.assertSessionLimit(TestConstants.USER_ID);

		verify(userSessionRedisStore).cleanupStaleActiveSessions(TestConstants.USER_ID);
		verify(userSessionRedisStore).syncOnlineUserAfterTerminate(TestConstants.USER_ID);
		verify(userSessionRedisStore, never()).countActiveSessions(anyLong());

		reset(userSessionRedisStore);
		when(userConfigProperties.getMaxSessionCount()).thenReturn(0);
		sessionLimitGuard.assertSessionLimit(TestConstants.USER_ID);
		verify(userSessionRedisStore).cleanupStaleActiveSessions(TestConstants.USER_ID);
		verify(userSessionRedisStore).syncOnlineUserAfterTerminate(TestConstants.USER_ID);
		verify(userSessionRedisStore, never()).countActiveSessions(anyLong());
	}

	@Test
	@DisplayName("会话上限：活跃会话数未达上限时放行")
	void assertSessionLimit_shouldReturn_whenCountBelowMax() {
		when(userConfigProperties.getMaxSessionCount()).thenReturn(3);
		when(userSessionRedisStore.countActiveSessions(TestConstants.USER_ID)).thenReturn(2L);

		sessionLimitGuard.assertSessionLimit(TestConstants.USER_ID);

		verify(userSessionRedisStore).cleanupStaleActiveSessions(TestConstants.USER_ID);
		verify(userSessionRedisStore).syncOnlineUserAfterTerminate(TestConstants.USER_ID);
		verify(userSessionRedisStore).countActiveSessions(TestConstants.USER_ID);
		verify(userSessionRedisStore, never()).evictOldestActiveSession(anyLong());
	}

	@Test
	@DisplayName("会话上限：策略为空或 REJECT_LOGIN 时抛 MAX_SESSION_LIMIT_EXCEEDED")
	void assertSessionLimit_shouldThrow_whenStrategyNullOrRejectLogin() {
		when(userConfigProperties.getMaxSessionCount()).thenReturn(1);
		when(userSessionRedisStore.countActiveSessions(TestConstants.USER_ID)).thenReturn(1L);

		when(userConfigProperties.getSessionLimitStrategy()).thenReturn(null);
		assertEquals(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED, assertThrows(AuthBusinessException.class,
				() -> sessionLimitGuard.assertSessionLimit(TestConstants.USER_ID))
			.getResultCode());

		reset(userConfigProperties, userSessionRedisStore);
		when(userConfigProperties.getMaxSessionCount()).thenReturn(1);
		when(userSessionRedisStore.countActiveSessions(TestConstants.USER_ID)).thenReturn(1L);
		when(userConfigProperties.getSessionLimitStrategy()).thenReturn(SessionLimitStrategy.REJECT_LOGIN);
		assertEquals(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED, assertThrows(AuthBusinessException.class,
				() -> sessionLimitGuard.assertSessionLimit(TestConstants.USER_ID))
			.getResultCode());
	}

	@Test
	@DisplayName("会话上限：EVICT_OLDEST 时执行淘汰并重算，仍超限则抛异常")
	void assertSessionLimit_shouldEvictOldest_andRecount_whenEVICT_OLDEST() {
		when(userConfigProperties.getMaxSessionCount()).thenReturn(1);
		when(userConfigProperties.getSessionLimitStrategy()).thenReturn(SessionLimitStrategy.EVICT_OLDEST);
		when(userSessionRedisStore.countActiveSessions(TestConstants.USER_ID)).thenReturn(1L, 1L);
		when(userSessionRedisStore.evictOldestActiveSession(TestConstants.USER_ID)).thenReturn(Optional.of("old-jti"));

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> sessionLimitGuard.assertSessionLimit(TestConstants.USER_ID));

		assertEquals(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED, exception.getResultCode());
		verify(userSessionRedisStore).evictOldestActiveSession(TestConstants.USER_ID);
	}

}
