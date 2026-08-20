package com.auth.service.auth.support.login.ratelimit;

import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static com.auth.module.security.contract.redis.SecurityRedisKey.LOGIN_CODE_LIMIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RateLimitAspect 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	private DummyService proxyWithAspect() {
		RateLimitAspect aspect = new RateLimitAspect(redisTemplate);
		AspectJProxyFactory factory = new AspectJProxyFactory(new DummyService());
		factory.addAspect(aspect);
		return factory.getProxy();
	}

	@Test
	@DisplayName("限流：principal 为空时抛 BAD_REQUEST_MISSING_EMAIL_EXCEPTION")
	void checkRateLimit_shouldThrowMissingEmail_whenPrincipalBlank() {
		// Arrange
		DummyService proxy = proxyWithAspect();

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class, proxy::missingPrincipal);

		// Assert
		assertEquals(AuthResultCode.BAD_REQUEST_MISSING_EMAIL_EXCEPTION, exception.getResultCode());
	}

	@Test
	@DisplayName("限流：计数超过上限时抛 TOO_MANY_REQUESTS")
	void checkRateLimit_shouldThrowTooManyRequests_whenCountExceeded() {
		// Arrange
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment(anyString())).thenReturn(6L);
		DummyService proxy = proxyWithAspect();

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class, proxy::sendCode);

		// Assert
		assertEquals(AuthResultCode.TOO_MANY_REQUESTS, exception.getResultCode());
	}

	@Test
	@DisplayName("限流：SpEL #email 从方法参数解析 principal")
	void checkRateLimit_shouldParsePrincipalFromSpelEmailParameter() {
		// Arrange
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment(anyString())).thenReturn(1L);
		when(redisTemplate.expire(anyString(), any())).thenReturn(true);
		DummyService proxy = proxyWithAspect();
		String email = "user@example.com";

		// Act & Assert
		assertDoesNotThrow(() -> proxy.sendCodeWithEmail(email));
		verify(valueOperations).increment(LOGIN_CODE_LIMIT.key(email));
	}

	@Test
	@DisplayName("限流：SpEL 解析结果为空时抛 BAD_REQUEST_MISSING_EMAIL_EXCEPTION")
	void checkRateLimit_shouldThrowMissingEmail_whenSpelResolvesBlank() {
		// Arrange
		DummyService proxy = proxyWithAspect();

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> proxy.sendCodeWithEmail(null));

		// Assert
		assertEquals(AuthResultCode.BAD_REQUEST_MISSING_EMAIL_EXCEPTION, exception.getResultCode());
	}

	@Test
	@DisplayName("限流：首次计数时 expire 返回 false，抛 TOO_MANY_REQUESTS")
	void checkRateLimit_shouldThrowTooManyRequests_whenExpireFailedOnFirstCount() {
		// Arrange
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment(anyString())).thenReturn(1L);
		when(redisTemplate.expire(anyString(), any())).thenReturn(false);
		DummyService proxy = proxyWithAspect();

		// Act
		AuthBusinessException exception = assertThrows(AuthBusinessException.class, proxy::sendCode);

		// Assert
		assertEquals(AuthResultCode.TOO_MANY_REQUESTS, exception.getResultCode());
	}

	static class DummyService {

		@RateLimit()
		void missingPrincipal() {
			// fixture: body intentionally empty; aspect intercepts before invocation
		}

		@RateLimit(principal = "user@example.com")
		void sendCode() {
			// fixture: body intentionally empty; aspect intercepts before invocation
		}

		@RateLimit(principal = "#email")
		void sendCodeWithEmail(String email) {
			// fixture: body intentionally empty; aspect intercepts before invocation
		}

	}

}
