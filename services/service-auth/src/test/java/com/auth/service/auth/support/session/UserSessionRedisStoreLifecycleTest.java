package com.auth.service.auth.support.session;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.module.security.contract.redis.SecurityRedisKey;
import com.auth.service.auth.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link UserSessionRedisStore} 会话生命周期与在线用户索引单元测试
 */
@DisplayName("UserSessionRedisStore 会话生命周期")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserSessionRedisStoreLifecycleTest {

	private static final String ONLINE_USERS_KEY = SecurityRedisKey.ONLINE_USERS.fixedKey();

	@Mock
	private RedisTemplate<String, String> sessionRedisTemplate;

	@Mock
	private UserSessionRedisScripts userSessionRedisScripts;

	@Mock
	private DefaultRedisScript<Long> registerScript;

	@Mock
	private DefaultRedisScript<Long> terminateScript;

	@Mock
	private DefaultRedisScript<Long> terminateOrphanScript;

	@Mock
	private DefaultRedisScript<Long> rotateRefreshScript;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@Mock
	private SetOperations<String, String> setOperations;

	@Mock
	private ZSetOperations<String, String> zSetOperations;

	private UserSessionRedisStore userSessionRedisStore;

	@BeforeEach
	void setUp() {
		when(userSessionRedisScripts.registerScript()).thenReturn(registerScript);
		when(userSessionRedisScripts.terminateScript()).thenReturn(terminateScript);
		when(userSessionRedisScripts.terminateOrphanScript()).thenReturn(terminateOrphanScript);
		when(userSessionRedisScripts.rotateRefreshScript()).thenReturn(rotateRefreshScript);
		when(sessionRedisTemplate.opsForHash()).thenReturn(hashOperations);
		when(sessionRedisTemplate.opsForSet()).thenReturn(setOperations);
		when(sessionRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
		OnlineUserZSetStore onlineUserZSetStore = new OnlineUserZSetStore(sessionRedisTemplate);
		userSessionRedisStore = new UserSessionRedisStore(sessionRedisTemplate, userSessionRedisScripts,
				onlineUserZSetStore);
	}

	@Test
	@DisplayName("registerSession：Lua 写入 Hash、用户 Set 与在线用户 ZSet")
	void registerSession_shouldExecuteRegisterScript() {
		UserSessionIndex index = sampleSessionIndex();
		when(sessionRedisTemplate.execute(eq(registerScript), anyList(), any())).thenReturn(1L);

		userSessionRedisStore.registerSession(index);

		verify(sessionRedisTemplate).execute(eq(registerScript),
				eq(List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
						SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), ONLINE_USERS_KEY)),
				any(Object[].class));
	}

	@Test
	@DisplayName("terminateSession：Lua 删除 Hash 与用户 Set，并在无会话时移出在线用户 ZSet")
	void terminateSession_shouldExecuteTerminateScriptAndSyncOnlineUser() {
		when(sessionRedisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(0L);

		userSessionRedisStore.terminateSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(sessionRedisTemplate).execute(terminateScript,
				List.of(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI),
						SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)),
				TestConstants.JTI);
		verify(zSetOperations).remove(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("terminateAllSessions：逐个 Lua 终止用户全部会话并移出在线用户 ZSet")
	void terminateAllSessions_shouldTerminateEachSessionAndRemoveOnlineUser() {
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "other-jti"));
		when(sessionRedisTemplate.execute(eq(terminateScript), anyList(), any())).thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(0L);

		userSessionRedisStore.terminateAllSessions(TestConstants.USER_ID);

		verify(sessionRedisTemplate).delete(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID));
		verify(sessionRedisTemplate, times(2)).execute(eq(terminateScript), anyList(), anyString());
		verify(zSetOperations).remove(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("listUserSessions：Hash 不存在时清理 Set 且不返回")
	void listUserSessions_shouldCleanupStaleIndexAndSkipMissingHash() {
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "stale-jti"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI))).thenReturn(Map.of("userId",
				String.valueOf(TestConstants.USER_ID), "sessionId", TestConstants.JTI, "loginAt", "1700000000000"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("stale-jti"))).thenReturn(Map.of());
		when(setOperations.remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti"))
			.thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(1L);

		var sessions = userSessionRedisStore.listUserSessions(TestConstants.USER_ID);

		assertEquals(1, sessions.size());
		assertEquals(TestConstants.JTI, sessions.get(0).getSessionId());
		verify(setOperations).remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti");
	}

	@Test
	@DisplayName("syncOnlineUserAfterTerminate：仍有活跃会话时不移出 ZSet")
	void syncOnlineUserAfterTerminate_shouldKeepUserWhenSessionsRemain() {
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(2L);

		userSessionRedisStore.syncOnlineUserAfterTerminate(TestConstants.USER_ID);

		verify(zSetOperations, never()).remove(anyString(), anyString());
	}

	@Test
	@DisplayName("registerSession：缺少 sessionId 或 userId 时抛异常")
	void registerSession_shouldRejectMissingIdentifiers() {
		UserSessionIndex index = new UserSessionIndex();
		index.setSessionId(" ");
		index.setUserId(TestConstants.USER_ID);

		assertThrows(IllegalArgumentException.class, () -> userSessionRedisStore.registerSession(index));
		verifyNoInteractions(sessionRedisTemplate);
	}

	@Test
	@DisplayName("rotateRefresh：Lua 返回 ROTATED 时不读 last* 字段")
	void rotateRefresh_shouldReturnRotated_whenScriptReturnsOne() {
		when(sessionRedisTemplate.execute(eq(rotateRefreshScript), anyList(), any(), any(), any(), any(), any(), any(),
				any(), any(), any()))
			.thenReturn(1L);

		var result = userSessionRedisStore.rotateRefresh(sampleRotateCommand(System.currentTimeMillis() + 3_600_000));

		assertEquals(RefreshRotateOutcome.ROTATED, result.outcome());
		verify(hashOperations, never()).multiGet(anyString(), anyList());
	}

	@Test
	@DisplayName("rotateRefresh：Lua 返回 REUSED 时读取 last* 并组装结果")
	void rotateRefresh_shouldReturnReusedTokens_whenScriptReturnsTwo() {
		long accessExpiresAt = System.currentTimeMillis() + 3_600_000;
		when(sessionRedisTemplate.execute(eq(rotateRefreshScript), anyList(), any(), any(), any(), any(), any(), any(),
				any(), any(), any()))
			.thenReturn(2L);
		when(hashOperations.multiGet(eq(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI)), anyList()))
			.thenReturn(List.of("cached-access", "cached-refresh", String.valueOf(accessExpiresAt)));

		var result = userSessionRedisStore.rotateRefresh(sampleRotateCommand(accessExpiresAt));

		assertEquals(RefreshRotateOutcome.REUSED, result.outcome());
		assertEquals("cached-access", result.accessToken());
		assertEquals("cached-refresh", result.refreshToken());
		assertEquals(accessExpiresAt, result.accessExpiresAt().toEpochMilli());
	}

	/**
	 * 构造带 loginAt 的样例会话索引
	 * @return 会话索引
	 */
	private UserSessionIndex sampleSessionIndex() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		index.setLoginAt(1_700_000_000_000L);
		index.setRefreshTokenExpiresAt(System.currentTimeMillis() + Duration.ofHours(2).toMillis());
		return index;
	}

	/**
	 * 构造旋转入参样例
	 * @param accessExpiresAtMs access 过期时间
	 * @return 旋转入参
	 */
	private RefreshRotateCommand sampleRotateCommand(long accessExpiresAtMs) {
		return RefreshRotateCommand.builder()
			.jti(TestConstants.JTI)
			.requestRefreshHash("req-hash")
			.newRefreshHash("new-hash")
			.newRefreshExpiresAtMs(System.currentTimeMillis() + 60_000)
			.ttl(Duration.ofSeconds(120))
			.graceMs(30_000L)
			.accessToken("access")
			.refreshToken("refresh")
			.accessExpiresAtMs(accessExpiresAtMs)
			.build();
	}

}
