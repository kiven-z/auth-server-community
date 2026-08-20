package com.auth.service.auth.support.login;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RefreshTokenCookieWriter 单元测试
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenCookieWriterTest {

	private static final String COOKIE_NAME = RefreshTokenCookieWriter.REFRESH_TOKEN_COOKIE_NAME;

	@Mock
	private JwtProperties jwtProperties;

	private RefreshTokenCookieWriter writer;

	@BeforeEach
	void setUp() {
		writer = new RefreshTokenCookieWriter(jwtProperties);
	}

	@Test
	@DisplayName("readToken：Cookie 为 null 时返回空字符串")
	void readToken_shouldReturnEmpty_whenCookiesIsNull() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getCookies()).thenReturn(null);

		String result = writer.readToken(request);

		assertEquals("", result);
	}

	@Test
	@DisplayName("readToken：存在目标 Cookie 时返回对应值")
	void readToken_shouldReturnValue_whenCookieExists() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie target = new Cookie(COOKIE_NAME, "test-refresh-token");
		when(request.getCookies()).thenReturn(new Cookie[] { target });

		String result = writer.readToken(request);

		assertEquals("test-refresh-token", result);
	}

	@Test
	@DisplayName("writeToken：refreshToken 为 blank 时不写入 Cookie")
	void writeToken_shouldSkip_whenTokenIsBlank() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		writer.writeToken(response, request, "", 3600L);

		verify(response, never()).addHeader(anyString(), anyString());
	}

	@Test
	@DisplayName("writeToken：持久 Cookie 写入 Max-Age")
	void writeToken_shouldIncludeMaxAge_whenPersistent() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.isSecure()).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);

		writer.writeToken(response, request, "refresh-value", 604800L);

		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		String cookieStr = headerValue.getValue();
		assertTrue(cookieStr.contains("auth-refresh-token=refresh-value"));
		assertTrue(cookieStr.contains("Max-Age=604800"));
	}

	@Test
	@DisplayName("writeToken：会话 Cookie 不写入持久 Max-Age（Spring 对 -1 通常省略 Max-Age 属性）")
	void writeToken_shouldUseSessionMaxAge_whenNotRemembered() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.isSecure()).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);

		writer.writeToken(response, request, "refresh-value", RefreshTokenCookieWriter.SESSION_COOKIE_MAX_AGE_SECONDS);

		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		String cookieStr = headerValue.getValue();
		assertTrue(cookieStr.contains("auth-refresh-token=refresh-value"));
		assertFalse(cookieStr.matches(".*Max-Age=[1-9]\\d*.*"));
	}

	@Test
	@DisplayName("clearToken：应清除同名 Cookie，Max-Age=0")
	void clearToken_shouldDeleteCookie() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.isSecure()).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);

		writer.clearToken(response, request);

		ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
		verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerValue.capture());
		assertTrue(headerValue.getValue().contains("Max-Age=0"));
	}

}
