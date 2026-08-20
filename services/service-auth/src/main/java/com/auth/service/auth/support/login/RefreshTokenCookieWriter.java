package com.auth.service.auth.support.login;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * 刷新令牌 Cookie 读写及「记住我」策略
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RefreshTokenCookieWriter {

	/**
	 * Refresh Cookie 名称
	 */
	public static final String REFRESH_TOKEN_COOKIE_NAME = "auth-refresh-token";

	/**
	 * Spring {@link ResponseCookie} 会话级 Max-Age（浏览器关闭后失效）
	 */
	public static final long SESSION_COOKIE_MAX_AGE_SECONDS = -1L;

	/**
	 * 一天的秒数
	 */
	private static final long SECONDS_PER_DAY = 86_400L;

	private final JwtProperties jwtProperties;

	/**
	 * 按「记住我」策略写入 Refresh Cookie
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 * @param refreshToken 刷新令牌
	 * @param rememberMe 是否记住登录
	 */
	public void writeRefreshToken(HttpServletResponse response, HttpServletRequest request, String refreshToken,
			boolean rememberMe) {
		long maxAgeSeconds = !rememberMe ? SESSION_COOKIE_MAX_AGE_SECONDS : jwtProperties.getRefreshExpired();
		writeToken(response, request, refreshToken, maxAgeSeconds);
	}

	/**
	 * 解析响应中的「阅读/记住」天数
	 * @return 天数（不足一天按 1 天计）
	 */
	public long resolveReadMeDay() {
		long refreshExpiredSeconds = jwtProperties.getRefreshExpired();
		if (refreshExpiredSeconds <= 0) {
			return 1L;
		}
		return (refreshExpiredSeconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY;
	}

	/**
	 * 从 Cookie 中读取刷新令牌，不存在时返回空字符串交由业务层统一抛错
	 * @param request HTTP 请求
	 * @return 刷新令牌
	 */
	public String readToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (ArrayUtil.isEmpty(cookies)) {
			return "";
		}
		Optional<Cookie> refreshCookie = Arrays.stream(cookies)
			.filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
			.findFirst();
		return refreshCookie.map(Cookie::getValue).orElse("");
	}

	/**
	 * 下发 HttpOnly 刷新令牌 Cookie
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 * @param refreshToken 刷新令牌
	 * @param maxAgeSeconds Max-Age（秒）；{@link #SESSION_COOKIE_MAX_AGE_SECONDS} 表示会话 Cookie
	 */
	public void writeToken(HttpServletResponse response, HttpServletRequest request, String refreshToken,
			long maxAgeSeconds) {
		if (CharSequenceUtil.isBlank(refreshToken)) {
			return;
		}
		ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
			.httpOnly(true)
			.secure(resolveSecureFlag(request))
			.sameSite("Lax")
			.path("/api/auth")
			.maxAge(maxAgeSeconds)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	/**
	 * 清除 HttpOnly 刷新令牌 Cookie
	 * @param response HTTP 响应
	 * @param request HTTP 请求
	 */
	public void clearToken(HttpServletResponse response, HttpServletRequest request) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
			.httpOnly(true)
			.secure(resolveSecureFlag(request))
			.sameSite("Lax")
			.path("/api/auth")
			.maxAge(0)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	/**
	 * 在 HTTPS 或网关透传 HTTPS 协议头时启用 Secure
	 * @param request HTTP 请求
	 * @return 是否开启 Secure
	 */
	private boolean resolveSecureFlag(HttpServletRequest request) {
		if (request.isSecure()) {
			return true;
		}
		String forwardedProto = request.getHeader("X-Forwarded-Proto");
		return CharSequenceUtil.equalsIgnoreCase(forwardedProto, "https");
	}

}
