package com.auth.module.security.contract.dto.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * {@link AuthorizationInvalidateRequest} 反序列化：按外层 kind 解析 payload。
 *
 * @author Bunny
 */
public class AuthorizationInvalidateRequestDeserializer extends JsonDeserializer<AuthorizationInvalidateRequest> {

	/**
	 * 根据 kind 节点类型，转换为对应的 AuthorizationInvalidatePayload 子类
	 * @param context 反序列化上下文
	 * @param kind kind 节点类型
	 * @param payloadNode payload 节点
	 * @return 对应的 AuthorizationInvalidatePayload 子类
	 * @throws IOException 如果转换失败
	 */
	private static AuthorizationInvalidatePayload resolvePayload(DeserializationContext context,
			AuthorizationChangeKind kind, JsonNode payloadNode) throws IOException {
		return switch (kind) {
			case ROLE -> context.readTreeAsValue(payloadNode, RoleInvalidatePayload.class);
			case PERMISSION -> context.readTreeAsValue(payloadNode, PermissionInvalidatePayload.class);
			case GRANT -> context.readTreeAsValue(payloadNode, GrantInvalidatePayload.class);
			case USER_DEPT -> context.readTreeAsValue(payloadNode, UserDeptInvalidatePayload.class);
			case USER_POST -> context.readTreeAsValue(payloadNode, UserPostInvalidatePayload.class);
			case USER -> context.readTreeAsValue(payloadNode, UserInvalidatePayload.class);
		};
	}

	@Override
	public AuthorizationInvalidateRequest deserialize(JsonParser parser, DeserializationContext context)
			throws IOException {
		// 读取根节点
		JsonNode node = parser.getCodec().readTree(parser);
		String eventId = node.path("eventId").asText(null);
		JsonNode kindNode = node.get("kind");
		// 校验 kind 节点不能为空
		if (kindNode == null || kindNode.isNull()) {
			throw new IllegalArgumentException("kind must not be null");
		}

		// 将 kind 节点转换为 AuthorizationChangeKind 枚举
		AuthorizationChangeKind kind = AuthorizationChangeKind.valueOf(kindNode.asText());
		// 读取 payload 节点
		JsonNode payloadNode = node.get("payload");
		// 校验 payload 节点不能为空
		if (payloadNode == null || payloadNode.isNull()) {
			throw new IllegalArgumentException("payload must not be null");
		}

		// 根据 kind 节点类型，转换为对应的 AuthorizationInvalidatePayload 子类
		AuthorizationInvalidatePayload payload = resolvePayload(context, kind, payloadNode);
		return new AuthorizationInvalidateRequest(eventId, kind, payload);
	}

}
