package com.auth.module.security.contract.dto;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OperationLogIngestRequest} 与 {@link OperationLogPayloadEvent} 互转单元测试
 *
 * @author Bunny
 */
class OperationLogIngestRequestTest {

	@Test
	@DisplayName("fromPayload 与 toPayload 应保留 targetId")
	void roundTrip_shouldPreserveTargetId() {
		OperationLogPayloadEvent original = OperationLogPayloadEvent.builder()
			.userId(1L)
			.username("alice")
			.operationType("QUERY")
			.module("SYSTEM:X")
			.targetType("Dept")
			.targetId(55L)
			.requestMethod("GET")
			.requestUri("/d/55")
			.requestParams(null)
			.responseStatus(200)
			.responseMessage(null)
			.executionTimeMs(3)
			.ipAddress("127.0.0.1")
			.userAgent(null)
			.className("C")
			.methodName("get")
			.build();

		OperationLogIngestRequest req = OperationLogIngestRequest.fromPayload(original);
		assertEquals(55L, req.getTargetId());
		assertEquals("alice", req.getUsername());

		OperationLogPayloadEvent back = req.toPayload();
		assertEquals(55L, back.getTargetId());
		assertEquals("alice", back.getUsername());
	}

	@Test
	@DisplayName("targetId 为空时应保持 null")
	void roundTrip_shouldAllowNullTargetId() {
		OperationLogPayloadEvent original = OperationLogPayloadEvent.builder()
			.userId(1L)
			.operationType("QUERY")
			.module("SYSTEM:X")
			.targetType("Dept")
			.requestMethod("GET")
			.requestUri("/d")
			.requestParams(null)
			.responseStatus(200)
			.responseMessage(null)
			.executionTimeMs(3)
			.ipAddress("127.0.0.1")
			.userAgent(null)
			.className("C")
			.methodName("list")
			.build();

		OperationLogIngestRequest req = OperationLogIngestRequest.fromPayload(original);
		assertNull(req.getTargetId());
		assertNull(req.toPayload().getTargetId());
	}

}
