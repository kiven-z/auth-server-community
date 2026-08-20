package com.auth.service.auth.support.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_PERM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthProfileRedisCache} 单元测试
 */
@DisplayName("AuthProfileRedisCache 授权画像缓存")
@ExtendWith(MockitoExtension.class)
class AuthProfileRedisCacheTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ObjectMapper objectMapper;

	private AuthProfileRedisCache cache;

	@BeforeEach
	void setUp() {
		cache = new AuthProfileRedisCache(redisTemplate, objectMapper);
	}

	@Test
	@DisplayName("evictProfiles：delete 返回 null 时按 0 处理")
	void evictProfiles_shouldReturnZeroWhenDeleteReturnsNull() {
		// RedisTemplate.delete 可能返回 null，避免 NPE
		when(redisTemplate.delete(anyCollection())).thenReturn(null);

		int deleted = cache.evictProfiles(List.of(1L, 2L));

		assertEquals(0, deleted);
		verify(redisTemplate).delete(List.of(USER_PERM.key(1L), USER_PERM.key(2L)));
	}

	@Test
	@DisplayName("evictProfiles：返回实际删除数量")
	void evictProfiles_shouldReturnDeletedCount() {
		when(redisTemplate.delete(anyCollection())).thenReturn(2L);

		int deleted = cache.evictProfiles(List.of(1L, 2L));

		assertEquals(2, deleted);
	}

	@Test
	@DisplayName("evictProfiles：空集合直接返回 0")
	void evictProfiles_shouldReturnZeroForEmptyInput() {
		assertEquals(0, cache.evictProfiles(List.of()));
	}

}
