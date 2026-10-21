package com.auth.common.web.exception;

import com.auth.common.core.model.response.Result;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 最低优先级的兜底异常处理
 *
 * @author Bunny
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class FallbackExceptionHandler extends BaseExceptionResponseBuilder {

	private static final Pattern METHOD_NOT_SUPPORTED_PATTERN = Pattern
		.compile("Request method '(.*?)' is not supported");

	@ExceptionHandler({ ServletException.class, Exception.class })
	public ResponseEntity<Result<Object>> handleAny(Exception exception) {
		// 方法不支持异常
		if (exception instanceof HttpRequestMethodNotSupportedException methodNotSupportedException) {
			String method = methodNotSupportedException.getMethod();
			return warn(HttpStatus.METHOD_NOT_ALLOWED, 405, CommonWebErrorCodes.METHOD_NOT_ALLOWED,
					"HTTP method not allowed" + ": " + method, exception, "Method not supported");
		}

		// 消息不支持异常
		String message = Objects.requireNonNullElse(exception.getMessage(), "Internal server error.");

		Matcher methodErrorMatcher = METHOD_NOT_SUPPORTED_PATTERN.matcher(message);
		// 方法不支持异常
		if (methodErrorMatcher.find()) {
			return warn(HttpStatus.METHOD_NOT_ALLOWED, 405, CommonWebErrorCodes.METHOD_NOT_ALLOWED,
					"HTTP method not allowed: " + methodErrorMatcher.group(1), exception, "Method not supported");
		}

		return error(HttpStatus.INTERNAL_SERVER_ERROR, 500, CommonWebErrorCodes.INTERNAL_ERROR,
				"Internal server error.", exception, "Unhandled exception");
	}

}
