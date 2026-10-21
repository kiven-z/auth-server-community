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
 * 将 JJWT 异常映射为本模块异常
 *
 * @author Bunny
 */
@UtilityClass
public class JwtExceptionTranslator {

	public static com.auth.common.jwt.exception.JwtException translate(Throwable throwable) {
		Throwable t = throwable;
		while (t.getCause() != null && t != t.getCause()) {
			if (t instanceof ExpiredJwtException) {
				break;
			}
			t = t.getCause();
		}
		if (t instanceof ExpiredJwtException ex) {
			return new JwtExpiredException("JWT has expired.", ex);
		}
		if (t instanceof MalformedJwtException || t instanceof UnsupportedJwtException) {
			return new JwtParseException("Malformed or unsupported JWT.", t);
		}
		if (t instanceof SecurityException) {
			return new InvalidTokenException("JWT signature verification failed.", t);
		}
		if (t instanceof io.jsonwebtoken.JwtException ex) {
			return new InvalidTokenException("Invalid JWT: " + ex.getMessage(), ex);
		}
		if (t instanceof IllegalArgumentException ex) {
			return new InvalidTokenException("Invalid JWT argument: " + ex.getMessage(), ex);
		}
		return new InvalidTokenException("Invalid JWT.", throwable);
	}

}
