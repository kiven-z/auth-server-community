package com.auth.common.web.exception;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

/**
 * 异常响应构建器基类 用于构建异常响应
 *
 * @author Bunny
 */
@Slf4j
public abstract class BaseExceptionResponseBuilder {

	/**
	 * 构建响应
	 * @param status 状态
	 * @param code 代码
	 * @param message 消息
	 * @return 响应实体
	 */
	protected ResponseEntity<Result<Object>> respond(HttpStatus status, Integer code, String message) {
		int finalCode = Objects.requireNonNullElse(code, status.value());
		String finalMessage = CharSequenceUtil.blankToDefault(message, status.getReasonPhrase());
		return ResponseEntity.status(status).body(Result.error(null, finalCode, finalMessage));
	}

	/**
	 * 构建警告响应
	 * @param status 状态
	 * @param code 代码
	 * @param message 消息
	 * @param exception 异常
	 * @param logMessage 日志消息
	 * @return 响应实体
	 */
	protected ResponseEntity<Result<Object>> warn(HttpStatus status, Integer code, String message, Throwable exception,
			String logMessage) {
		log.warn("{}: {}", logMessage, exception.getMessage(), exception);
		return respond(status, code, message);
	}

	/**
	 * 构建错误响应
	 * @param status 状态
	 * @param code 代码
	 * @param message 消息
	 * @param exception 异常
	 * @param logMessage 日志消息
	 * @return 响应实体
	 */
	protected ResponseEntity<Result<Object>> error(HttpStatus status, Integer code, String message, Throwable exception,
			String logMessage) {
		log.error("{}: {}", logMessage, exception.getMessage(), exception);
		return respond(status, code, message);
	}

}
