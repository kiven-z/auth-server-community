package com.auth.module.security.starter.service;

import com.auth.module.security.autoconfigure.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_PERM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthProfileCacheServiceTest {

	private ValueOperations<String, Object> valueOps;

	private ObjectMapper objectMapper;

	private AuthProfileCacheService cacheService;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
		valueOps = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		objectMapper = mock(ObjectMapper.class);
		cacheService = new AuthProfileCacheService(redisTemplate, objectMapper);
	}

	@Test
	@DisplayName("测试直接返回AuthProfile时返回配置")
	void load_returnsProfileDirectly_whenRawIsAuthProfile() {
		AuthProfile expected = AuthProfile.builder().userId(100L).username("alice").build();
		when(valueOps.get(USER_PERM.key(100L))).thenReturn(expected);

		AuthProfile result = cacheService.load(100L);
		assertNotNull(result);
		assertEquals(100L, result.getUserId());
		assertEquals("alice", result.getUsername());
	}

	@Test
	@DisplayName("测试转换Map时应返回配置")
	void load_convertsMap_whenRawIsNotAuthProfile() {
		Map<String, Object> raw = Map.of("userId", 100, "username", "alice", "permVersion", 42);
		when(valueOps.get(USER_PERM.key(100L))).thenReturn(raw);

		AuthProfile converted = AuthProfile.builder().userId(100L).username("alice").permVersion(42L).build();
		when(objectMapper.convertValue(raw, AuthProfile.class)).thenReturn(converted);

		AuthProfile result = cacheService.load(100L);
		assertNotNull(result);
		assertEquals(100L, result.getUserId());
		assertEquals(42L, result.getPermVersion());
	}

	@Test
	@DisplayName("测试不在Redis中时应返回空")
	void load_returnsNull_whenNotInRedis() {
		when(valueOps.get(USER_PERM.key(100L))).thenReturn(null);

		assertNull(cacheService.load(100L));
	}

	@Test
	@DisplayName("测试Redis失败时应抛出异常")
	void load_throws_whenRedisFails() {
		when(valueOps.get(USER_PERM.key(100L))).thenThrow(new RuntimeException("Connection refused"));

		SecurityTokenException ex = assertThrows(SecurityTokenException.class, () -> cacheService.load(100L));
		assertEquals(SecurityResultCodeEnum.REDIS_UNAVAILABLE, ex.getResultCode());
	}

	@Test
	@DisplayName("测试反序列化失败时应抛出异常")
	void load_throws_whenDeserializationFails() {
		Map<String, Object> raw = Map.of("invalid", "data");
		when(valueOps.get(USER_PERM.key(100L))).thenReturn(raw);
		when(objectMapper.convertValue(raw, AuthProfile.class))
			.thenThrow(new IllegalArgumentException("Cannot deserialize"));

		SecurityTokenException ex = assertThrows(SecurityTokenException.class, () -> cacheService.load(100L));
		assertEquals(SecurityResultCodeEnum.PROFILE_DESERIALIZE_FAILED, ex.getResultCode());
	}

}
