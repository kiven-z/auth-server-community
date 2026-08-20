package com.auth.service.auth.support.token;

import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginFailureRateLimiter 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginFailureRateLimiterTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private UserConfigProperties userConfigProperties;

	@Test
	@DisplayName("登录前置检查：失败次数达到阈值时抛 USER_LOCKED_WITH_MINUTES（分钟>=1）")
	void assertNotLocked_shouldThrowLockedWithMinutes_whenFailuresReachMax() {
		// Arrange
		when(userConfigProperties.getMaxPasswordAttempts()).thenReturn(5);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(5L);
		when(redisTemplate.getExpire(anyString(), eq(TimeUnit.MINUTES))).thenReturn(0L);

		LoginFailureRateLimiter limiter = new LoginFailureRateLimiter(redisTemplate, userConfigProperties);

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> limiter.assertNotLocked(TestConstants.USER_ID));

		// Assert
		assertEquals(AuthResultCode.USER_LOCKED_WITH_MINUTES, exception.getResultCode());
		Object[] args = exception.getMessageArgs();
		assertNotNull(args);
		assertTrue(args.length >= 1);
		assertTrue(((Number) args[0]).longValue() >= 1);
	}

	@Test
	@DisplayName("失败计数：首次设置 ATTEMPT_TTL；达到阈值升级 LOCK_DURATION")
	void recordFailure_shouldSetAttemptTtlOnFirst_andLockTtlOnReachMax() {
		// Arrange
		when(userConfigProperties.getMaxPasswordAttempts()).thenReturn(2);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		LoginFailureRateLimiter limiter = new LoginFailureRateLimiter(redisTemplate, userConfigProperties);

		// Case 1: first failure -> count=1
		when(valueOperations.increment(anyString())).thenReturn(1L);
		int remainingAfterFirst = limiter.recordFailure(TestConstants.USER_ID);
		assertEquals(1, remainingAfterFirst);
		verify(redisTemplate).expire(anyString(), any());

		// Case 2: reach max -> count=2
		reset(redisTemplate, valueOperations);
		when(userConfigProperties.getMaxPasswordAttempts()).thenReturn(2);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment(anyString())).thenReturn(2L);
		int remainingAfterSecond = limiter.recordFailure(TestConstants.USER_ID);
		assertEquals(0, remainingAfterSecond);
		verify(redisTemplate).expire(anyString(), any());
	}

	@Test
	@DisplayName("成功记录：删除失败计数 key")
	void recordSuccess_shouldDeleteKey() {
		// Arrange
		LoginFailureRateLimiter limiter = new LoginFailureRateLimiter(redisTemplate, userConfigProperties);

		// Act
		limiter.recordSuccess(TestConstants.USER_ID);

		// Assert
		verify(redisTemplate).delete(anyString());
	}

}
