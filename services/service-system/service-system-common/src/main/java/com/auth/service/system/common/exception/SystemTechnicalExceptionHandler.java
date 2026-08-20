package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.common.jwt.exception.JwtExpiredException;
import com.auth.common.jwt.exception.JwtParseException;
import com.auth.common.web.exception.BaseExceptionResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.JWT_INVALID;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.REDIS_SYSTEM_ERROR;

/**
 * 系统技术异常处理器
 *
 * @author Bunny
 */
@Slf4j
@Order(20)
@RestControllerAdvice
public class SystemTechnicalExceptionHandler extends BaseExceptionResponseBuilder {

	private final MessageSource messageSource;

	public SystemTechnicalExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * 处理 JWT 异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler({ JwtExpiredException.class, JwtParseException.class })
	public ResponseEntity<Result<Object>> handleJwtException(RuntimeException exception) {
		String fallback = Objects.requireNonNullElse(exception.getMessage(), "Invalid JWT token.");
		Result<Object> body = SystemResultResponseFactory.build(messageSource, JWT_INVALID, fallback);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	/**
	 * 处理 Redis 异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(RedisSystemException.class)
	public ResponseEntity<Result<Object>> handleRedisException(RedisSystemException exception) {
		String fallback = Objects.requireNonNullElse(exception.getMessage(), "Redis service unavailable.");
		Result<Object> body = SystemResultResponseFactory.build(messageSource, REDIS_SYSTEM_ERROR, fallback);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
	}

}
