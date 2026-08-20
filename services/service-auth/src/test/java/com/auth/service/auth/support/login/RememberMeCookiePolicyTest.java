package com.auth.service.auth.support.login;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * {@link RefreshTokenCookieWriter} remember-me 策略单元测试
 */
@ExtendWith(MockitoExtension.class)
class RememberMeCookiePolicyTest {

	@Mock
	private JwtProperties jwtProperties;

	@Test
	@DisplayName("readMeDay 由 refreshExpired 向上取整为天")
	void resolveReadMeDay_shouldCeilToDays() {
		when(jwtProperties.getRefreshExpired()).thenReturn(604800L);
		RefreshTokenCookieWriter writer = new RefreshTokenCookieWriter(jwtProperties);

		assertEquals(7L, writer.resolveReadMeDay());
	}

	@Test
	@DisplayName("refreshExpired 非法时 readMeDay 至少为 1")
	void resolveReadMeDay_shouldDefaultToOne_whenRefreshExpiredNonPositive() {
		when(jwtProperties.getRefreshExpired()).thenReturn(0L);
		RefreshTokenCookieWriter writer = new RefreshTokenCookieWriter(jwtProperties);

		assertEquals(1L, writer.resolveReadMeDay());
	}

}
