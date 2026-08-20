package com.auth.module.security.autoconfigure.service;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.feign.SystemSecurityIngestFeignClient;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FeignRemoteOperationLogHandler} unit tests.
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class FeignRemoteOperationLogHandlerTest {

	@Mock
	private SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	@InjectMocks
	private FeignRemoteOperationLogHandler handler;

	private static OperationLogPayloadEvent samplePayload() {
		return OperationLogPayloadEvent.builder()
			.userId(1L)
			.operationType("QUERY")
			.module("M")
			.targetType("")
			.requestMethod("GET")
			.requestUri("/api/demo")
			.requestParams(null)
			.responseStatus(200)
			.responseMessage(null)
			.executionTimeMs(1)
			.ipAddress("127.0.0.1")
			.userAgent(null)
			.className("A")
			.methodName("b")
			.build();
	}

	@Test
	@DisplayName("handle should submit operation log via Feign without throwing")
	void handle_shouldSubmitViaFeign() {
		when(systemSecurityIngestFeignClient.appendOperationLog(any(OperationLogIngestRequest.class)))
			.thenReturn(Result.success());
		OperationLogPayloadEvent payload = samplePayload();

		// Feign 成功时不应向外抛出异常
		assertDoesNotThrow(() -> handler.handle(payload));

		ArgumentCaptor<OperationLogIngestRequest> captor = ArgumentCaptor.forClass(OperationLogIngestRequest.class);
		verify(systemSecurityIngestFeignClient).appendOperationLog(captor.capture());
		assertEquals(payload.getRequestUri(), captor.getValue().getRequestUri());
		assertEquals(payload.getModule(), captor.getValue().getModule());
	}

	@Test
	@DisplayName("handle should swallow Feign failures without propagating")
	void handle_shouldSwallowFeignFailure() {
		when(systemSecurityIngestFeignClient.appendOperationLog(any(OperationLogIngestRequest.class)))
			.thenThrow(new RuntimeException("feign down"));

		// 出站失败仅记录日志，不阻断调用方
		assertDoesNotThrow(() -> handler.handle(samplePayload()));
	}

	@Test
	@DisplayName("handle should swallow non-success Result without propagating")
	void handle_shouldSwallowNonSuccessResult() {
		when(systemSecurityIngestFeignClient.appendOperationLog(any(OperationLogIngestRequest.class)))
			.thenReturn(Result.error("upstream error"));

		assertDoesNotThrow(() -> handler.handle(samplePayload()));
	}

}
