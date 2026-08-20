package com.auth.service.auth.exception;

import com.auth.common.core.model.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证服务业务异常处理：统一使用 {@link AuthBusinessExceptionResultBuilder} 与 {@link MessageSource} 拼装
 * {@link Result}
 *
 * @author Bunny
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class AuthExceptionHandler {

	private final MessageSource messageSource;

	public AuthExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(AuthBusinessException.class)
	public ResponseEntity<Result<Object>> handleAuthBusinessException(AuthBusinessException exception) {
		return toResponse(exception);
	}

	/**
	 * 凭证错误：映射为与用户名密码错误一致的业务码与响应结构
	 * @param exception 原始异常
	 * @return 响应实体
	 */
	@ExceptionHandler(AuthBadCredentialsException.class)
	public ResponseEntity<Result<Object>> handleBadCredentialsException(AuthBadCredentialsException exception) {
		log.warn("Bad credentials exception: {}", exception.getMessage());
		return toResponse(new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	/**
	 * 将 {@link AuthBusinessException} 转为 HTTP 响应；resultCode 为空时降级为
	 * {@link AuthResultCode#SERVER_ERROR}
	 * @param exception 业务异常
	 * @return 响应实体
	 */
	private ResponseEntity<Result<Object>> toResponse(AuthBusinessException exception) {
		AuthResultCode resultCode = exception.getResultCode();
		if (resultCode == null) {
			log.warn("Auth business exception without resultCode", exception);
			AuthBusinessException fallback = new AuthBusinessException(AuthResultCode.SERVER_ERROR);
			Result<Object> body = AuthBusinessExceptionResultBuilder.build(messageSource, fallback);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
		}

		Result<Object> body = AuthBusinessExceptionResultBuilder.build(messageSource, exception);
		log.warn("Auth business exception: {} ({})", resultCode.getError(), resultCode.getCode(), exception);
		return ResponseEntity.status(AuthBusinessExceptionResultBuilder.resolveHttpStatus(resultCode)).body(body);
	}

}
