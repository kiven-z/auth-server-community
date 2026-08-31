package com.auth.common.web.exception;

import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FallbackExceptionHandler} 单元测试
 */
@DisplayName("FallbackExceptionHandler 兜底错误映射")
class FallbackExceptionHandlerTest {

	private final FallbackExceptionHandler handler = new FallbackExceptionHandler();

	@Test
	@DisplayName("未捕获异常返回 INTERNAL_ERROR，不泄露异常原文")
	void handleAny_unhandled_returnsInternalError() {
		ResponseEntity<Result<Object>> response = handler.handleAny(new RuntimeException("secret stack detail"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(500);
		assertThat(response.getBody().getError()).isEqualTo(CommonWebErrorCodes.INTERNAL_ERROR);
		assertThat(response.getBody().getMessage()).isEqualTo("Internal server error.");
		assertThat(response.getBody().getMessage()).doesNotContain("secret");
	}

	@Test
	@DisplayName("HTTP 方法不支持返回 METHOD_NOT_ALLOWED")
	void handleAny_methodNotSupported_returnsMethodNotAllowed() {
		HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("DELETE");

		ResponseEntity<Result<Object>> response = handler.handleAny(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(405);
		assertThat(response.getBody().getError()).isEqualTo(CommonWebErrorCodes.METHOD_NOT_ALLOWED);
		assertThat(response.getBody().getMessage()).contains("DELETE");
	}

}
