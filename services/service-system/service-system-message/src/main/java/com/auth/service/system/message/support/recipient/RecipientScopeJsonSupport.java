package com.auth.service.system.message.support.recipient;

import com.auth.common.core.utils.JsonSupport;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * 接收范围与 JSON 快照互转
 *
 * @author Bunny
 */
@UtilityClass
public class RecipientScopeJsonSupport {

	private static final String KEY_IDS = "ids";

	private static final String KEY_INCLUDE_CHILDREN = "includeChildren";

	/**
	 * 序列化范围快照
	 * @param scope 接收范围
	 * @return JSON 字符串或
	 */
	public static String toJson(RecipientScope scope) {
		return switch (scope.getType()) {
			case ALL -> null;
			case USER, POST -> JsonSupport.toJson(Map.of(KEY_IDS, scope.safeIds()));
			case DEPT -> {
				Map<String, Object> payload = new LinkedHashMap<>();
				payload.put(KEY_IDS, scope.safeIds());
				payload.put(KEY_INCLUDE_CHILDREN, scope.includeChildrenOrDefault());
				yield JsonSupport.toJson(payload);
			}
		};
	}

	/**
	 * 从任务快照还原接收范围（Worker 派发用）
	 * @param scopeType 范围类型
	 * @param json recipient_scope_json
	 * @return 接收范围
	 */
	public static RecipientScope fromJson(RecipientScopeType scopeType, String json) {
		if (scopeType == RecipientScopeType.ALL) {
			return RecipientScope.builder().type(RecipientScopeType.ALL).build();
		}
		List<Long> ids = parseIds(json);
		Boolean includeChildren = null;
		if (scopeType == RecipientScopeType.DEPT) {
			includeChildren = parseIncludeChildren(json);
		}
		return RecipientScope.builder().type(scopeType).ids(ids).includeChildren(includeChildren).build();
	}

	/**
	 * 解析 JSON（测试/回显用）
	 * @param json JSON 字符串
	 * @return ids；解析失败返回空列表
	 */
	public static List<Long> parseIds(String json) {
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		JsonNode idsNode = JsonSupport.readObjectTree(json).get(KEY_IDS);
		if (idsNode == null || !idsNode.isArray()) {
			return Collections.emptyList();
		}

		return StreamSupport.stream(idsNode.spliterator(), false).map(JsonNode::asLong).toList();
	}

	/**
	 * 解析部门是否含子树；字段缺失或不合法时返回 null
	 * @param json JSON 字符串
	 * @return includeChildren；无有效字段时为 null
	 */
	@Nullable
	public static Boolean parseIncludeChildren(String json) {
		if (json == null || json.isBlank()) {
			return null;
		}
		JsonNode node = JsonSupport.readObjectTree(json).get(KEY_INCLUDE_CHILDREN);
		if (node == null || node.isNull() || !node.isBoolean()) {
			return null;
		}
		return node.asBoolean();
	}

}
