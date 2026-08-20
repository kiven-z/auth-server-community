package com.auth.common.jwt.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JwtProperties} 配置校验单元测试
 */
@DisplayName("JwtProperties 配置校验")
class JwtPropertiesTest {

	private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab";

	private static JwtProperties hmacProperties(long accessExpired, long refreshExpired) {
		JwtProperties properties = new JwtProperties();
		properties.setIssuer("test-issuer");
		properties.setSecret(TEST_SECRET);
		properties.setAccessExpired(accessExpired);
		properties.setRefreshExpired(refreshExpired);
		return properties;
	}

	@Test
	@DisplayName("refresh-expired 大于 access-expired 时校验通过")
	void validate_shouldPass_whenRefreshExpiredGreaterThanAccessExpired() {
		JwtProperties properties = hmacProperties(3600L, 7200L);

		assertDoesNotThrow(properties::validate);
		assertEquals(3600L, properties.getAccessExpired());
	}

	@Test
	@DisplayName("refresh-expired 小于等于 access-expired 时校验失败")
	void validate_shouldFail_whenRefreshExpiredNotGreaterThanAccessExpired() {
		JwtProperties equal = hmacProperties(3600L, 3600L);
		IllegalStateException equalEx = assertThrows(IllegalStateException.class, equal::validate);
		assertEquals("auth.common.jwt.refresh-expired must be greater than auth.common.jwt.access-expired.",
				equalEx.getMessage());

		JwtProperties less = hmacProperties(7200L, 3600L);
		IllegalStateException lessEx = assertThrows(IllegalStateException.class, less::validate);
		assertEquals("auth.common.jwt.refresh-expired must be greater than auth.common.jwt.access-expired.",
				lessEx.getMessage());
	}

	@Test
	@DisplayName("HS256 缺少 secret 时校验失败")
	void validate_shouldFail_whenHmacSecretMissing() {
		JwtProperties properties = hmacProperties(3600L, 7200L);
		properties.setSecret(null);

		IllegalStateException ex = assertThrows(IllegalStateException.class, properties::validate);
		assertEquals("auth.common.jwt.secret is required for HS256.", ex.getMessage());
	}

}
