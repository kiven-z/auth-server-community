package com.auth.module.security.starter.security.authenticate;

import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.module.security.autoconfigure.pipeline.authenticate.SessionCountChecker;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSION;
import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSIONS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionCountCheckerTest {

	private RedisTemplate<String, String> sessionRedisTemplate;

	private SetOperations<String, String> setOps;

	private UserConfigProperties userConfigProperties;

	private SessionCountChecker checker;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		sessionRedisTemplate = mock(RedisTemplate.class);
		setOps = mock(SetOperations.class);
		when(sessionRedisTemplate.opsForSet()).thenReturn(setOps);
		userConfigProperties = new UserConfigProperties();
		checker = new SessionCountChecker(sessionRedisTemplate, userConfigProperties);
	}

	// ---- verifySessionPresent ----

	@Test
	@DisplayName("测试会话存在时应成功")
	void verifySessionPresent_success() {
		when(sessionRedisTemplate.hasKey(USER_SESSION.key("jti-1"))).thenReturn(true);
		assertDoesNotThrow(() -> checker.verifySessionPresent("jti-1"));
	}

	@Test
	@DisplayName("测试会话缺失时应抛出会话缺失异常")
	void verifySessionPresent_sessionMissing() {
		when(sessionRedisTemplate.hasKey(USER_SESSION.key("jti-1"))).thenReturn(false);
		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> checker.verifySessionPresent("jti-1"));
		assertEquals(SecurityResultCodeEnum.SESSION_MISSING, ex.getResultCode());
	}

	@Test
	@DisplayName("测试空会话ID时应抛出令牌无效异常")
	void verifySessionPresent_blankSessionId() {
		assertThrows(SecurityTokenException.class, () -> checker.verifySessionPresent(""));
		assertThrows(SecurityTokenException.class, () -> checker.verifySessionPresent(null));
	}

	// ---- verifyConcurrentSessionsWithinLimit ----

	@Test
	@DisplayName("测试会话在集合中时应成功")
	void verifyConcurrentSessionsWithinLimit_sessionInSet() {
		String indexKey = USER_SESSIONS.key(100L);
		when(sessionRedisTemplate.hasKey(indexKey)).thenReturn(true);
		when(setOps.size(indexKey)).thenReturn(2L);
		when(setOps.isMember(indexKey, "jti-1")).thenReturn(true);

		assertDoesNotThrow(() -> checker.verifyConcurrentSessionsWithinLimit(100L, "jti-1"));
	}

	@Test
	@DisplayName("测试会话不在集合中时应抛出会话缺失异常")
	void verifyConcurrentSessionsWithinLimit_sessionNotInSet() {
		String indexKey = USER_SESSIONS.key(100L);
		when(sessionRedisTemplate.hasKey(indexKey)).thenReturn(true);
		when(setOps.size(indexKey)).thenReturn(2L);
		when(setOps.isMember(indexKey, "jti-1")).thenReturn(false);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> checker.verifyConcurrentSessionsWithinLimit(100L, "jti-1"));
		assertEquals(SecurityResultCodeEnum.SESSION_MISSING, ex.getResultCode());
	}

	@Test
	@DisplayName("测试索引键不存在时应成功")
	void verifyConcurrentSessionsWithinLimit_indexKeyNotExist() {
		String indexKey = USER_SESSIONS.key(100L);
		when(sessionRedisTemplate.hasKey(indexKey)).thenReturn(false);

		assertDoesNotThrow(() -> checker.verifyConcurrentSessionsWithinLimit(100L, "jti-1"));
	}

	@Test
	@DisplayName("测试空会话ID时应抛出令牌无效异常")
	void verifyConcurrentSessionsWithinLimit_blankSessionId() {
		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> checker.verifyConcurrentSessionsWithinLimit(100L, ""));
		assertEquals(SecurityResultCodeEnum.TOKEN_INVALID, ex.getResultCode());
	}

	@Test
	@DisplayName("测试会话数量超过最大值时应抛出会话数量超过最大值异常")
	void verifyConcurrentSessionsWithinLimit_sizeExceedsMax_throws() {
		userConfigProperties.setMaxSessionCount(3);
		String indexKey = USER_SESSIONS.key(100L);
		when(sessionRedisTemplate.hasKey(indexKey)).thenReturn(true);
		when(setOps.size(indexKey)).thenReturn(5L);
		when(setOps.isMember(indexKey, "jti-1")).thenReturn(true);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> checker.verifyConcurrentSessionsWithinLimit(100L, "jti-1"));
		assertEquals(SecurityResultCodeEnum.SESSION_LIMIT_EXCEEDED, ex.getResultCode());
	}

	@Test
	@DisplayName("测试集合为空时应抛出会话不一致异常")
	void verifyConcurrentSessionsWithinLimit_setEmpty_throws() {
		String indexKey = USER_SESSIONS.key(100L);
		when(sessionRedisTemplate.hasKey(indexKey)).thenReturn(true);
		when(setOps.size(indexKey)).thenReturn(0L);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> checker.verifyConcurrentSessionsWithinLimit(100L, "jti-1"));
		assertEquals(SecurityResultCodeEnum.SESSION_INCONSISTENT, ex.getResultCode());
	}

	@Test
	@DisplayName("测试集合大小为空时应抛出会话不一致异常")
	void verifyConcurrentSessionsWithinLimit_setSizeNull_throws() {
		String indexKey = USER_SESSIONS.key(100L);
		when(sessionRedisTemplate.hasKey(indexKey)).thenReturn(true);
		when(setOps.size(indexKey)).thenReturn(null);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> checker.verifyConcurrentSessionsWithinLimit(100L, "jti-1"));
		assertEquals(SecurityResultCodeEnum.SESSION_INCONSISTENT, ex.getResultCode());
	}

}
