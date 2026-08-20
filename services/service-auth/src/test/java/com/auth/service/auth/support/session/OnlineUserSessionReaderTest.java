package com.auth.service.auth.support.session;

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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OnlineUserSessionReader} 在线用户读侧单元测试
 */
@DisplayName("OnlineUserSessionReader 在线用户读侧")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnlineUserSessionReaderTest {

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
	private SetOperations<String, String> setOperations;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@Mock
	private ZSetOperations<String, String> zSetOperations;

	private OnlineUserSessionReader reader;

	@BeforeEach
	void setUp() {
		when(userSessionRedisScripts.registerScript()).thenReturn(registerScript);
		when(userSessionRedisScripts.terminateScript()).thenReturn(terminateScript);
		when(userSessionRedisScripts.terminateOrphanScript()).thenReturn(terminateOrphanScript);
		when(userSessionRedisScripts.rotateRefreshScript()).thenReturn(rotateRefreshScript);
		when(sessionRedisTemplate.opsForSet()).thenReturn(setOperations);
		when(sessionRedisTemplate.opsForHash()).thenReturn(hashOperations);
		when(sessionRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
		OnlineUserZSetStore onlineUserZSetStore = new OnlineUserZSetStore(sessionRedisTemplate);
		UserSessionRedisStore repository = new UserSessionRedisStore(sessionRedisTemplate, userSessionRedisScripts,
				onlineUserZSetStore);
		reader = new OnlineUserSessionReader(repository, onlineUserZSetStore);
	}

	@Test
	@DisplayName("pageOnlineUsers：无活跃会话时清理 ZSet 且不纳入结果")
	void pageOnlineUsers_shouldCleanupStaleOnlineUserAndSkip() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(2L);
		when(zSetOperations.reverseRange(ONLINE_USERS_KEY, 0L, 63L))
			.thenReturn(new LinkedHashSet<>(List.of(String.valueOf(TestConstants.USER_ID), "999")));
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(1L);
		when(zSetOperations.score(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID)))
			.thenReturn(1_700_000_000_000D);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(999L))).thenReturn(0L);

		var slice = reader.pageOnlineUsers(1, 10, null);

		assertEquals(1L, slice.total());
		assertEquals(1, slice.users().size());
		assertEquals(TestConstants.USER_ID, slice.users().get(0).userId());
		verify(zSetOperations).remove(ONLINE_USERS_KEY, "999");
	}

	@Test
	@DisplayName("pageOnlineUsers：支持用户 ID 筛选")
	void pageOnlineUsers_shouldApplyUserFilter() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(1L);
		when(zSetOperations.reverseRange(ONLINE_USERS_KEY, 0L, 63L))
			.thenReturn(Set.of(String.valueOf(TestConstants.USER_ID), "99"));
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(2L);
		when(zSetOperations.score(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID)))
			.thenReturn(1_700_000_000_000D);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(99L))).thenReturn(1L);
		when(zSetOperations.score(ONLINE_USERS_KEY, "99")).thenReturn(1_700_000_000_100D);

		var slice = reader.pageOnlineUsers(1, 10, Set.of(TestConstants.USER_ID));

		assertEquals(1L, slice.total());
		assertEquals(1, slice.users().size());
		assertEquals(TestConstants.USER_ID, slice.users().get(0).userId());
		assertEquals(2, slice.users().get(0).activeSessionCount());
	}

	@Test
	@DisplayName("pageOnlineUsers：在线用户 ZSet 为空时返回空切片")
	void pageOnlineUsers_shouldReturnEmptyWhenOnlineZsetEmpty() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(0L);

		var slice = reader.pageOnlineUsers(1, 10, null);

		assertEquals(0L, slice.total());
		assertTrue(slice.users().isEmpty());
	}

	@Test
	@DisplayName("pageOnlineUsers：Set 含僵尸 jti 时清理后移出 ZSet 且不纳入结果")
	void pageOnlineUsers_shouldCleanupZombieSessionsBeforeCount() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(1L);
		when(zSetOperations.reverseRange(ONLINE_USERS_KEY, 0L, 63L))
			.thenReturn(Set.of(String.valueOf(TestConstants.USER_ID)));
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of("stale-jti"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("stale-jti"))).thenReturn(Map.of());
		when(setOperations.remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti"))
			.thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(0L);

		var slice = reader.pageOnlineUsers(1, 10, null);

		assertEquals(0L, slice.total());
		assertTrue(slice.users().isEmpty());
		verify(zSetOperations).remove(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID));
	}

	@Test
	@DisplayName("pageOnlineUsers：清理僵尸 jti 后仍保留有效会话用户")
	void pageOnlineUsers_shouldKeepUserWhenValidSessionsRemainAfterCleanup() {
		when(zSetOperations.zCard(ONLINE_USERS_KEY)).thenReturn(1L);
		when(zSetOperations.reverseRange(ONLINE_USERS_KEY, 0L, 63L))
			.thenReturn(Set.of(String.valueOf(TestConstants.USER_ID)));
		when(setOperations.members(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID)))
			.thenReturn(Set.of(TestConstants.JTI, "stale-jti"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key(TestConstants.JTI))).thenReturn(Map.of("userId",
				String.valueOf(TestConstants.USER_ID), "sessionId", TestConstants.JTI, "loginAt", "1700000000000"));
		when(hashOperations.entries(SecurityRedisKey.USER_SESSION.key("stale-jti"))).thenReturn(Map.of());
		when(setOperations.remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti"))
			.thenReturn(1L);
		when(setOperations.size(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID))).thenReturn(1L);
		when(zSetOperations.score(ONLINE_USERS_KEY, String.valueOf(TestConstants.USER_ID)))
			.thenReturn(1_700_000_000_000D);

		var slice = reader.pageOnlineUsers(1, 10, null);

		assertEquals(1L, slice.total());
		assertEquals(1, slice.users().size());
		assertEquals(TestConstants.USER_ID, slice.users().get(0).userId());
		assertEquals(1, slice.users().get(0).activeSessionCount());
		verify(setOperations).remove(SecurityRedisKey.USER_SESSIONS.key(TestConstants.USER_ID), "stale-jti");
	}

}
