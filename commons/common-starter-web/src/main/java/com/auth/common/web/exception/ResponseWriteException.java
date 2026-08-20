package com.auth.common.web.exception;

/**
 * 响应写入异常
 *
 * @author Bunny
 */
public class ResponseWriteException extends RuntimeException {

	public ResponseWriteException(String message, Throwable cause) {
		super(message, cause);
	}

}
