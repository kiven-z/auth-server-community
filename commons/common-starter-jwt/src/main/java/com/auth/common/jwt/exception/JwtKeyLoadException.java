package com.auth.common.jwt.exception;

/**
 * 密钥文件缺失、密码错误、别名错误等
 *
 * @author Bunny
 */
public class JwtKeyLoadException extends JwtException {

	public JwtKeyLoadException(String message) {
		super("JWT_KEY_LOAD_FAILED", message);
	}

	public JwtKeyLoadException(String message, Throwable cause) {
		super("JWT_KEY_LOAD_FAILED", message, cause);
	}

}
