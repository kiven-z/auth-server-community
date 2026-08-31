package com.auth.common.web.exception;

import com.auth.common.core.exception.RemoteServiceException;
import com.auth.common.core.model.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 远端 Feign 业务错误 HTTP 映射
 *
 * @author Bunny
 */
@Slf4j
@Order(9)
@RestControllerAdvice
public class RemoteServiceExceptionHandler {

	private static Result<Object> castResult(Result<?> result) {
		Result<Object> body = new Result<>();
		body.setCode(result.getCode());
		body.setMessage(result.getMessage());
		body.setError(result.getError());
		body.setSubCode(result.getSubCode());
		body.setData(result.getData());
		body.setExt(result.getExt());
		body.setTimestamp(result.getTimestamp());
		return body;
	}

	/**
	 * 处理远端服务业务错误
	 * @param exception 远端异常
	 * @return 响应实体
	 */
	@ExceptionHandler(RemoteServiceException.class)
	public ResponseEntity<Result<Object>> handleRemoteService(RemoteServiceException exception) {
		log.warn("Remote service exception: {}", exception.getMessage(), exception);

		HttpStatus status = HttpStatus.resolve(exception.getHttpStatus());
		if (status == null) {
			status = HttpStatus.BAD_GATEWAY;
		}

		Result<?> result = exception.getResult();
		Result<Object> body = result == null
				? Result.error(status.value(), CommonWebErrorCodes.UPSTREAM_UNAVAILABLE, "Remote service error")
				: castResult(result);
		return ResponseEntity.status(status).body(body);
	}

}
