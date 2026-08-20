package com.auth.service.system.authorization.outbox;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Outbox payload 列与契约 Payload 的 JSON 编解码。
 *
 * @author Bunny
 */
@Component
public class InvalidationOutboxPayloadCodec {

	private final ObjectMapper objectMapper;

	public InvalidationOutboxPayloadCodec(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * 序列化失效业务键为 JSON。
	 * @param payload 业务键
	 * @return JSON 字符串
	 */
	public String serialize(AuthorizationInvalidatePayload payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Failed to serialize invalidation payload", ex);
		}
	}

	/**
	 * 按变更维度反序列化 JSON 为类型化 Payload。
	 * @param kind 变更维度
	 * @param json payload 列 JSON
	 * @return 业务键
	 */
	public AuthorizationInvalidatePayload deserialize(AuthorizationChangeKind kind, String json) {
		try {
			return switch (kind) {
				case ROLE -> objectMapper.readValue(json, RoleInvalidatePayload.class);
				case PERMISSION -> objectMapper.readValue(json, PermissionInvalidatePayload.class);
				case GRANT -> objectMapper.readValue(json, GrantInvalidatePayload.class);
				case USER_DEPT -> objectMapper.readValue(json, UserDeptInvalidatePayload.class);
				case USER_POST -> objectMapper.readValue(json, UserPostInvalidatePayload.class);
				case USER -> objectMapper.readValue(json, UserInvalidatePayload.class);
			};
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Failed to deserialize invalidation payload for kind=" + kind, ex);
		}
	}

}
