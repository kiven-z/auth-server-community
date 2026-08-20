package com.auth.module.security.contract.dto;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link AuthorizationAuditIngestRequest} 单元测试
 *
 * @author Bunny
 */
class AuthorizationAuditIngestRequestTest {

	@Test
	@DisplayName("fromEvent 与 toPayloadEvent 往返后关键字段一致")
	void roundTrip_shouldPreserveCoreFields() {
		Instant ts = Instant.parse("2024-01-02T03:04:05Z");
		SecurityAuthorizationAuditPayloadEvent original = SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(SecurityEventType.GRANTED)
			.userId(42L)
			.username("alice")
			.requestIp("10.0.0.1")
			.requestMethod("GET")
			.requestUri("/api/x")
			.requiredAuthority("sys:a:b")
			.decisionReason("MATCHED")
			.exceptionMessage(null)
			.className("C")
			.methodName("m")
			.methodParams(Map.of("id", 1))
			.timestamp(ts)
			.build();

		AuthorizationAuditIngestRequest dto = AuthorizationAuditIngestRequest.fromEvent(original);
		SecurityAuthorizationAuditPayloadEvent restored = dto.toPayloadEvent();

		assertEquals(SecurityEventType.GRANTED, restored.getEventType());
		assertEquals(42L, restored.getUserId());
		assertEquals("alice", restored.getUsername());
		assertEquals("10.0.0.1", restored.getRequestIp());
		assertEquals("GET", restored.getRequestMethod());
		assertEquals("/api/x", restored.getRequestUri());
		assertEquals("sys:a:b", restored.getRequiredAuthority());
		assertEquals("MATCHED", restored.getDecisionReason());
		assertEquals("C", restored.getClassName());
		assertEquals("m", restored.getMethodName());
		assertEquals(1, restored.getMethodParams().get("id"));
		assertEquals(ts, restored.getTimestamp());
	}

	@Test
	@DisplayName("toPayloadEvent 在 eventType 为空时应回落为 DENIED")
	void toPayloadEvent_whenEventTypeNull_shouldDefaultDenied() {
		AuthorizationAuditIngestRequest dto = new AuthorizationAuditIngestRequest();
		dto.setEventType(null);
		dto.setUserId(1L);

		SecurityAuthorizationAuditPayloadEvent event = dto.toPayloadEvent();

		assertEquals(SecurityEventType.DENIED, event.getEventType());
		assertNotNull(event.getTimestamp());
	}

}
