package com.auth.common.web.exception;

import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ValidationExceptionHandler} 单元测试
 */
class ValidationExceptionHandlerTest {

	private ValidationExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new ValidationExceptionHandler();
	}

	@Test
	@DisplayName("BindException：类型转换失败应返回 invalid value format")
	void handleBindException_typeMismatchLong_returnsInvalidValueFormatMessage() {
		BindException exception = bindExceptionWithTypeMismatch("abc");

		ResponseEntity<Result<Object>> response = handler.handleBindException(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(400, response.getBody().getCode());
		assertEquals(CommonWebErrorCodes.VALIDATION_FAILED, response.getBody().getError());
		assertEquals(CommonWebErrorCodes.VALIDATION_FAILED, response.getBody().getSubCode());
		assertEquals("Validation failed: jobId: invalid value format", response.getBody().getMessage());
	}

	@Test
	@DisplayName("MethodArgumentNotValidException：由 BindException 统一处理")
	void handleBindException_methodArgumentNotValid_returnsInvalidValueFormatMessage() throws NoSuchMethodException {
		DummyQuery target = new DummyQuery();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "logJobQuery");
		bindingResult.addError(new FieldError("logJobQuery", "jobId", "xyz", false,
				new String[] { "typeMismatch.jobId", "typeMismatch.java.lang.Long", "typeMismatch" },
				new Object[] { "jobId", Long.class }, "Failed to convert"));
		Method endpoint = ValidationExceptionHandlerTest.class.getDeclaredMethod("dummyEndpoint", DummyQuery.class);
		MethodParameter parameter = new MethodParameter(endpoint, 0);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

		ResponseEntity<Result<Object>> response = handler.handleBindException(exception);

		assertEquals(CommonWebErrorCodes.VALIDATION_FAILED, response.getBody().getError());
		assertEquals("Validation failed: jobId: invalid value format", response.getBody().getMessage());
	}

	@Test
	@DisplayName("MethodArgumentTypeMismatchException：单个参数类型错误应返回 invalid value format")
	void handleMethodArgumentTypeMismatch_longParam_returnsInvalidValueFormatMessage() {
		MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("abc", Long.class,
				"jobId", null, null);

		ResponseEntity<Result<Object>> response = handler.handleMethodArgumentTypeMismatch(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(CommonWebErrorCodes.VALIDATION_FAILED, response.getBody().getError());
		assertEquals("Validation failed: jobId: invalid value format", response.getBody().getMessage());
	}

	@Test
	@DisplayName("MissingServletRequestParameterException：必填参数缺失应返回 required")
	void handleServletRequestBinding_missingRequestParam_returnsRequiredMessage() {
		MissingServletRequestParameterException exception = new MissingServletRequestParameterException("taskType",
				"String");

		ResponseEntity<Result<Object>> response = handler.handleServletRequestBinding(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(400, response.getBody().getCode());
		assertEquals(CommonWebErrorCodes.VALIDATION_FAILED, response.getBody().getError());
		assertEquals("Validation failed: taskType: required", response.getBody().getMessage());
	}

	@Test
	@DisplayName("BindException：普通校验消息应原样输出英文字段提示")
	void handleBindException_constraintMessage_preservesDefaultMessage() {
		DummyQuery target = new DummyQuery();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "form");
		bindingResult.addError(new FieldError("form", "name", null, false, new String[] { "NotBlank.form.name" }, null,
				"must not be blank"));
		BindException exception = new BindException(bindingResult);

		ResponseEntity<Result<Object>> response = handler.handleBindException(exception);

		assertEquals(CommonWebErrorCodes.VALIDATION_FAILED, response.getBody().getError());
		assertEquals("Validation failed: name: must not be blank", response.getBody().getMessage());
	}

	@SuppressWarnings("unused")
	private void dummyEndpoint(DummyQuery query) {
		// 仅用于构造 MethodArgumentNotValidException 测试
	}

	private BindException bindExceptionWithTypeMismatch(String rejectedValue) {
		DummyQuery target = new DummyQuery();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "logJobQuery");
		bindingResult.addError(new FieldError(
				"logJobQuery", "jobId", rejectedValue, false, new String[] { "typeMismatch.logJobQuery.jobId",
						"typeMismatch.jobId", "typeMismatch.java.lang.Long", "typeMismatch" },
				new Object[] { "jobId", Long.class }, "Failed to convert"));
		return new BindException(bindingResult);
	}

	/**
	 * 测试用查询对象
	 */
	private static class DummyQuery {

	}

}
