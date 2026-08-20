package com.auth.common.jwt.exception;

/**
 * 签名无效、格式损坏、issuer 不匹配等
 *
 * @author Bunny
 */
public class InvalidTokenException extends JwtException {

	public InvalidTokenException(String message) {
		super("INVALID_TOKEN", message);
	}

	public InvalidTokenException(String message, Throwable cause) {
		super("INVALID_TOKEN", message, cause);
	}

}
