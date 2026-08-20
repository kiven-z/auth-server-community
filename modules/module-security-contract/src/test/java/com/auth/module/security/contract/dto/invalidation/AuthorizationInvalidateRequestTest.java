package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AuthorizationInvalidateRequest} 契约测试
 *
 * @author Bunny
 */
@DisplayName("AuthorizationInvalidateRequest 授权失效契约")
class AuthorizationInvalidateRequestTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("ROLE 请求 JSON 反序列化后 kind 与 payload 一致")
	void deserialize_rolePayload_shouldMatchKind() throws Exception {
		String json = """
				{
				  "eventId": "evt-role-001",
				  "kind": "ROLE",
				  "payload": {
				    "roleCodes": ["ADMIN", "OPERATOR"]
				  }
				}
				""";

		AuthorizationInvalidateRequest request = objectMapper.readValue(json, AuthorizationInvalidateRequest.class);

		assertEquals("evt-role-001", request.eventId());
		assertEquals(AuthorizationChangeKind.ROLE, request.kind());
		assertInstanceOf(RoleInvalidatePayload.class, request.payload());
		assertEquals(List.of("ADMIN", "OPERATOR"), ((RoleInvalidatePayload) request.payload()).roleCodes());
	}

	@Test
	@DisplayName("GRANT 请求 JSON 反序列化后主体键正确")
	void deserialize_grantPayload_shouldParseSubjects() throws Exception {
		String json = """
				{
				  "eventId": "evt-grant-001",
				  "kind": "GRANT",
				  "payload": {
				    "subjects": [
				      { "subjectType": "USER", "subjectId": 1 }
				    ]
				  }
				}
				""";

		AuthorizationInvalidateRequest request = objectMapper.readValue(json, AuthorizationInvalidateRequest.class);

		assertEquals(AuthorizationChangeKind.GRANT, request.kind());
		GrantInvalidatePayload payload = (GrantInvalidatePayload) request.payload();
		assertEquals(1, payload.subjects().size());
		assertEquals(GrantTableSubjectType.USER, payload.subjects().get(0).subjectType());
		assertEquals(1L, payload.subjects().get(0).subjectId());
	}

	@Test
	@DisplayName("序列化后再反序列化应保持字段一致")
	void roundTrip_rolePayload_shouldPreserveFields() throws Exception {
		AuthorizationInvalidateRequest original = new AuthorizationInvalidateRequest("evt-rt-001",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("CUSTOM_R_ONE")));

		String json = objectMapper.writeValueAsString(original);
		AuthorizationInvalidateRequest restored = objectMapper.readValue(json, AuthorizationInvalidateRequest.class);

		assertEquals(original.eventId(), restored.eventId());
		assertEquals(original.kind(), restored.kind());
		assertEquals(((RoleInvalidatePayload) original.payload()).roleCodes(),
				((RoleInvalidatePayload) restored.payload()).roleCodes());
	}

	@Test
	@DisplayName("USER 请求 JSON 反序列化后 kind 与 payload 一致")
	void deserialize_userPayload_shouldMatchKind() throws Exception {
		String json = """
				{
				  "eventId": "evt-user-001",
				  "kind": "USER",
				  "payload": {
				    "userIds": [10, 20]
				  }
				}
				""";

		AuthorizationInvalidateRequest request = objectMapper.readValue(json, AuthorizationInvalidateRequest.class);

		assertEquals("evt-user-001", request.eventId());
		assertEquals(AuthorizationChangeKind.USER, request.kind());
		assertInstanceOf(UserInvalidatePayload.class, request.payload());
		assertEquals(List.of(10L, 20L), ((UserInvalidatePayload) request.payload()).userIds());
	}

	@Test
	@DisplayName("已废弃的 USER_SCOPE kind 应拒绝反序列化")
	void deserialize_legacyUserScopePayload_shouldReject() {
		String json = """
				{
				  "eventId": "evt-legacy-001",
				  "kind": "USER_SCOPE",
				  "payload": {
				    "userIds": [10]
				  }
				}
				""";

		assertThrows(IllegalArgumentException.class,
				() -> objectMapper.readValue(json, AuthorizationInvalidateRequest.class));
	}

	@Test
	@DisplayName("kind 与 payload 类型不一致时应拒绝构造")
	void constructor_kindPayloadMismatch_shouldThrow() {
		PermissionInvalidatePayload mismatchedPayload = new PermissionInvalidatePayload(List.of("sys:user:list"));
		assertThrows(IllegalArgumentException.class,
				() -> new AuthorizationInvalidateRequest("evt-bad", AuthorizationChangeKind.ROLE, mismatchedPayload));
	}

	@Test
	@DisplayName("eventId 为空时应拒绝构造")
	void constructor_blankEventId_shouldThrow() {
		RoleInvalidatePayload payload = new RoleInvalidatePayload(List.of("ADMIN"));
		assertThrows(IllegalArgumentException.class,
				() -> new AuthorizationInvalidateRequest("  ", AuthorizationChangeKind.ROLE, payload));
	}

}
