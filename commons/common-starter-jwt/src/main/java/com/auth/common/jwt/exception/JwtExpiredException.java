package com.auth.common.jwt.exception;

/**
 * Access / Refresh 过期（在 clock skew 之外）
 *
 * @author Bunny
 */
public class JwtExpiredException extends JwtException {

	public JwtExpiredException(String message) {
		super("JWT_EXPIRED", message);
	}

	public JwtExpiredException(String message, Throwable cause) {
		super("JWT_EXPIRED", message, cause);
	}

}
