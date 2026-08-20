package com.auth.service.system.schedule.exception;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.exception.SystemResultResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 定时任务模块业务异常 HTTP 映射
 *
 * @author Bunny
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class SysJobExceptionHandler {

	private final MessageSource messageSource;

	public SysJobExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * 处理定时任务业务异常
	 * @param exception 业务异常
	 * @return 响应实体
	 */
	@ExceptionHandler(SysJobException.class)
	public ResponseEntity<Result<Object>> handleSysJobException(SysJobException exception) {
		log.warn("Schedule job exception: {}", exception.toString(), exception);

		Result<Object> body = SystemResultResponseFactory.build(messageSource, exception.getResultCode(),
				exception.getMessageArgs());
		HttpStatus status = SystemResultResponseFactory.resolveHttpStatus(exception.getResultCode());
		return ResponseEntity.status(status).body(body);
	}

}
