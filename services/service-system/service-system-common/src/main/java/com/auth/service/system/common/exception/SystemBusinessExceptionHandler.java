package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 系统业务异常 HTTP 映射
 *
 * @author Bunny
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class SystemBusinessExceptionHandler {

	private final MessageSource messageSource;

	public SystemBusinessExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * 处理系统业务异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(SystemBusinessException.class)
	public ResponseEntity<Result<Object>> handleSystemBusiness(SystemBusinessException exception) {
		log.warn("System business exception: {}", exception.toString(), exception);

		Result<Object> body = SystemResultResponseFactory.build(messageSource, exception.getResultCode(),
				exception.getMessageArgs());
		HttpStatus status = SystemResultResponseFactory.resolveHttpStatus(exception.getResultCode());
		return ResponseEntity.status(status).body(body);
	}

}
