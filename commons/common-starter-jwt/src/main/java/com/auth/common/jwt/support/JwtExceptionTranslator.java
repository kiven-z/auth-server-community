package com.auth.common.jwt.support;

import com.auth.common.jwt.exception.InvalidTokenException;
import com.auth.common.jwt.exception.JwtExpiredException;
import com.auth.common.jwt.exception.JwtParseException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import lombok.experimental.UtilityClass;

/**
 * 将 JJWT 异常映射为本模块异常类型 将 JJWT 抛出的各类异常（过期、格式错误、签名错误等）统一转换为模块自定义异常
 *
 * @author Bunny
 */
@UtilityClass
public class JwtExceptionTranslator {

	/**
	 * 翻译异常
	 * @param throwable 异常
	 * @return 翻译后的异常
	 */
	public static com.auth.common.jwt.exception.JwtException translate(Throwable throwable) {
		Throwable t = throwable;
		while (t.getCause() != null && t != t.getCause()) {
			if (t instanceof ExpiredJwtException) {
				break;
			}
			t = t.getCause();
		}
		// 如果异常为 ExpiredJwtException，则返回 JwtExpiredException
		if (t instanceof ExpiredJwtException ex) {
			return new JwtExpiredException("JWT has expired.", ex);
		}
		// 如果异常为 MalformedJwtException 或 UnsupportedJwtException，则返回 JwtParseException
		if (t instanceof MalformedJwtException || t instanceof UnsupportedJwtException) {
			return new JwtParseException("Malformed or unsupported JWT.", t);
		}
		// 如果异常为 SecurityException，则返回 InvalidTokenException
		if (t instanceof SecurityException) {
			return new InvalidTokenException("JWT signature verification failed.", t);
		}
		// 如果异常为 io.jsonwebtoken.JwtException，则返回 InvalidTokenException
		if (t instanceof io.jsonwebtoken.JwtException ex) {
			return new InvalidTokenException("Invalid JWT: " + ex.getMessage(), ex);
		}
		// 如果异常为 IllegalArgumentException，则返回 InvalidTokenException
		if (t instanceof IllegalArgumentException ex) {
			return new InvalidTokenException("Invalid JWT argument: " + ex.getMessage(), ex);
		}
		return new InvalidTokenException("Invalid JWT.", throwable);
	}

}
