package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效统一请求（HTTP / Feign / Outbox 共用）。
 *
 * @param eventId 业务事件 ID（幂等键）
 * @param kind 变更维度，与 payload 类型必须一致
 * @param payload 类型化业务键
 * @author Bunny
 */
@JsonDeserialize(using = AuthorizationInvalidateRequestDeserializer.class)
public record AuthorizationInvalidateRequest(String eventId, AuthorizationChangeKind kind,
		AuthorizationInvalidatePayload payload) implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 紧凑构造：校验必填字段及 kind 与 payload 一致性。
	 * @param eventId 事件 ID
	 * @param kind 变更维度
	 * @param payload 业务键
	 */
	public AuthorizationInvalidateRequest {
		if (eventId == null || eventId.isBlank()) {
			throw new IllegalArgumentException("eventId must not be blank");
		}
		if (kind == null) {
			throw new IllegalArgumentException("kind must not be null");
		}
		if (payload == null) {
			throw new IllegalArgumentException("payload must not be null");
		}
		if (payload.kind() != kind) {
			throw new IllegalArgumentException("kind and payload type mismatch");
		}
	}

}
