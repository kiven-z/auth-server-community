package com.auth.service.auth.support.redis.store;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * {@link LoginVerificationCodeStore} 单元测试
 */
@DisplayName("LoginVerificationCodeStore 登录验证码存储")
@ExtendWith(MockitoExtension.class)
class LoginVerificationCodeStoreTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	private LoginVerificationCodeStore store;

	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		store = new LoginVerificationCodeStore(redisTemplate);
	}

	@Test
	@DisplayName("verifyAndConsume：摘要匹配时删除 key")
	void verifyAndConsume_shouldDeleteWhenDigestMatches() {
		String phone = "13800138000";
		String code = "123456";
		String key = SMS_CODE.key(phone);
		when(valueOperations.get(key)).thenReturn(MD5.create().digestHex16(code));

		store.verifyAndConsume(key, code);

		verify(redisTemplate).delete(key);
	}

	@Test
	@DisplayName("verifyAndConsume：摘要不匹配时抛出 AUTH_CODE_ERROR")
	void verifyAndConsume_shouldThrowWhenDigestMismatch() {
		String phone = "13800138000";
		String key = SMS_CODE.key(phone);
		when(valueOperations.get(key)).thenReturn("stale");

		AuthBusinessException ex = assertThrows(AuthBusinessException.class,
				() -> store.verifyAndConsume(key, "000000"));

		assertEquals(AuthResultCode.AUTH_CODE_ERROR, ex.getResultCode());
		verify(redisTemplate, never()).delete(anyString());
	}

}
