package com.auth.module.security.autoconfigure.web.exception;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.web.error.SecurityErrorResponseSupport;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证异常处理
 *
 * @author Bunny
 */
@Slf4j
@Order(20)
@RestControllerAdvice
public class SecurityExceptionHandler {

	/**
	 * 处理访问拒绝异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
	public ResponseEntity<Result<Object>> handleAccessDenied(Exception exception) {
		log.warn("Access denied: {}", exception.getMessage());

		SecurityResultCodeEnum accessDenied = SecurityResultCodeEnum.ACCESS_DENIED;
		Result<Object> body = SecurityErrorResponseSupport.build(accessDenied);

		return ResponseEntity.status(accessDenied.getHttpStatus()).body(body);
	}

}
