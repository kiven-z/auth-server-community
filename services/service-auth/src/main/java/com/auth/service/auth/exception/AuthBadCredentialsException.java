package com.auth.service.auth.exception;

/**
 * 用户名或密码错误异常
 *
 * @author Bunny
 */
public class AuthBadCredentialsException extends RuntimeException {

	public AuthBadCredentialsException(String message) {
		super(message);
	}

}
