package com.auth.common.web.exception;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.utils.ExceptionCauseUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 验证 / 请求体相关异常处理 此处理器必须具有最高优先级，以避免被通用回退吞噬
 *
 * @author Bunny
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler extends BaseExceptionResponseBuilder {

	/**
	 * 参数类型不匹配时的统一英文提示
	 */
	private static final String INVALID_VALUE_FORMAT = "invalid value format";

	/**
	 * 处理模型绑定与 @Valid 校验异常（含 GET 查询对象、请求体）
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<Result<Object>> handleBindException(BindException exception) {
		// Spring 绑定阶段类型转换失败时的错误码
		String typeMismatch = "typeMismatch";

		String collect = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.sorted(Comparator.comparing(FieldError::getField))
			.map(fieldError -> {
				String message;
				if (typeMismatch.equals(fieldError.getCode())) {
					message = INVALID_VALUE_FORMAT;
				}
				else if (fieldError.getDefaultMessage() != null) {
					message = fieldError.getDefaultMessage();
				}
				else {
					message = "invalid";
				}
				return fieldError.getField() + ": " + message;
			})
			.collect(Collectors.joining(", "));

		return validationFailed((collect), exception);
	}

	/**
	 * 处理单个请求参数类型不匹配（如 @RequestParam Long id）
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Result<Object>> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException exception) {
		return validationFailed(exception.getName() + ": " + INVALID_VALUE_FORMAT, exception);
	}

	/**
	 * 处理请求参数绑定异常（如必填 @RequestParam 缺失）
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler({ ServletRequestBindingException.class, })
	public ResponseEntity<Result<Object>> handleServletRequestBinding(ServletRequestBindingException exception) {
		String details;
		if (exception instanceof MissingServletRequestParameterException missingParam) {
			details = missingParam.getParameterName() + ": required";
		}
		else {
			details = Objects.requireNonNullElse(exception.getMessage(), "request parameter binding failed");
		}
		return validationFailed(details, exception);
	}

	/**
	 * 处理约束验证异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Result<Object>> handleConstraintViolation(ConstraintViolationException exception) {
		String details = exception.getConstraintViolations()
			.stream()
			.map(v -> v.getPropertyPath() + ": " + v.getMessage())
			.collect(Collectors.joining(", "));

		return validationFailed(details, exception);
	}

	/**
	 * 处理HTTP消息不可读异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Result<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
		Throwable root = ExceptionCauseUtil.rootCause(exception);
		String rootName = root != null ? root.getClass().getSimpleName() : "Unknown";

		String message;
		switch (rootName) {
			case "JsonParseException" -> message = "Malformed JSON: please check syntax.";
			case "InvalidFormatException" -> message = "Invalid JSON value format: please check field types.";
			case "MismatchedInputException" -> message = "JSON does not match the expected schema.";
			default -> message = "Request body is not readable JSON.";
		}

		return warn(HttpStatus.BAD_REQUEST, 400, message, exception, "Unreadable HTTP message");
	}

	/**
	 * 构建 400 校验失败响应
	 */
	private ResponseEntity<Result<Object>> validationFailed(String details, Throwable exception) {
		// 校验失败响应前缀
		String validationFailedPrefix = "Validation failed: ";

		return warn(HttpStatus.BAD_REQUEST, 400, validationFailedPrefix + details, exception, "Validation error");
	}

}
