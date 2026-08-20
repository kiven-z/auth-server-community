package com.auth.common.jwt.exception;

/**
 * JWT 解析失败异常（错误码 JWT_PARSE_ERROR）
 *
 * @author Bunny
 */
public class JwtParseException extends JwtException {

	public JwtParseException(String message, Throwable cause) {
		super("JWT_PARSE_ERROR", message, cause);
	}

}
