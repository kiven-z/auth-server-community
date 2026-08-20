package com.auth.common.core.exception;

import com.auth.common.core.model.response.Result;
import lombok.Getter;

/**
 * 远端服务返回业务错误（Feign 非 2xx 且 body 为 {@link Result}）
 *
 * @author Bunny
 */
@Getter
public class RemoteServiceException extends RuntimeException {

	private final int httpStatus;

	private final Result<?> result;

	public RemoteServiceException(int httpStatus, Result<?> result) {
		super(resolveMessage(result));
		this.httpStatus = httpStatus;
		this.result = result;
	}

	private static String resolveMessage(Result<?> result) {
		if (result == null) {
			return "Remote service error";
		}
		String message = result.getMessage();
		if (message != null && !message.isBlank()) {
			return message;
		}
		return "Remote service error";
	}

}
