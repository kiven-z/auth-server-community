package com.auth.common.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link JsonSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("JsonSupport JSON 辅助")
class JsonSupportTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("toJson(mapper)：将 Map 序列化为 JSON 字符串")
	void toJson_serializesValue() {
		String json = JsonSupport.toJson(objectMapper, Map.of("locale", "zh"));

		assertThat(json).contains("\"locale\"").contains("\"zh\"");
	}

	@Test
	@DisplayName("toJson()：使用默认 ObjectMapper 序列化")
	void toJson_defaultMapper_serializesValue() {
		String json = JsonSupport.toJson(Map.of("locale", "zh"));

		assertThat(json).contains("\"locale\"").contains("\"zh\"");
	}

	@Test
	@DisplayName("fromJson(mapper)：将 JSON 字符串反序列化为目标类型")
	void fromJson_deserializesValue() {
		@SuppressWarnings("unchecked")
		Map<String, String> value = JsonSupport.fromJson(objectMapper, "{\"locale\":\"zh\"}", Map.class);

		assertThat(value).containsEntry("locale", "zh");
	}

	@Test
	@DisplayName("fromJson()：使用默认 ObjectMapper 反序列化")
	void fromJson_defaultMapper_deserializesValue() {
		@SuppressWarnings("unchecked")
		Map<String, String> value = JsonSupport.fromJson("{\"locale\":\"zh\"}", Map.class);

		assertThat(value).containsEntry("locale", "zh");
	}

	@Test
	@DisplayName("readTree(mapper)：解析合法 JSON 为 JsonNode")
	void readTree_parsesValidJson() {
		JsonNode node = JsonSupport.readTree(objectMapper, "{\"locale\":\"zh\"}");

		assertThat(node.isObject()).isTrue();
		assertThat(node.get("locale").asText()).isEqualTo("zh");
	}

	@Test
	@DisplayName("readTree()：使用默认 ObjectMapper 解析合法 JSON")
	void readTree_defaultMapper_parsesValidJson() {
		JsonNode node = JsonSupport.readTree("{\"locale\":\"zh\"}");

		assertThat(node.isObject()).isTrue();
		assertThat(node.get("locale").asText()).isEqualTo("zh");
	}

	@Test
	@DisplayName("readTree(mapper)：非法 JSON 抛出 IllegalArgumentException")
	void readTree_rejectsInvalidJson() {
		assertThatThrownBy(() -> JsonSupport.readTree(objectMapper, "{invalid-json"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Failed to parse JSON");
	}

	@Test
	@DisplayName("readTree()：非法 JSON 抛出 IllegalArgumentException")
	void readTree_defaultMapper_rejectsInvalidJson() {
		assertThatThrownBy(() -> JsonSupport.readTree("{invalid-json")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Failed to parse JSON");
	}

	@Test
	@DisplayName("readObjectTree(mapper)：接受 JSON 对象")
	void readObjectTree_acceptsObject() {
		JsonNode node = JsonSupport.readObjectTree(objectMapper, "{\"locale\":\"zh\"}");

		assertThat(node.isObject()).isTrue();
		assertThat(node.get("locale").asText()).isEqualTo("zh");
	}

	@Test
	@DisplayName("readObjectTree()：使用默认 ObjectMapper 接受 JSON 对象")
	void readObjectTree_defaultMapper_acceptsObject() {
		JsonNode node = JsonSupport.readObjectTree("{\"locale\":\"zh\"}");

		assertThat(node.isObject()).isTrue();
		assertThat(node.get("locale").asText()).isEqualTo("zh");
	}

	@Test
	@DisplayName("readObjectTree(mapper)：非对象 JSON 抛出 IllegalArgumentException")
	void readObjectTree_rejectsNonObject() {
		assertThatThrownBy(() -> JsonSupport.readObjectTree(objectMapper, "[\"a\"]"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("JSON value must be an object");
	}

	@Test
	@DisplayName("readObjectTree()：非对象 JSON 抛出 IllegalArgumentException")
	void readObjectTree_defaultMapper_rejectsNonObject() {
		assertThatThrownBy(() -> JsonSupport.readObjectTree("[\"a\"]")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("JSON value must be an object");
	}

	@Test
	@DisplayName("fromJson(mapper)：非法 JSON 抛出 IllegalArgumentException")
	void fromJson_rejectsInvalidJson() {
		assertThatThrownBy(() -> JsonSupport.fromJson(objectMapper, "{bad", Map.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Failed to deserialize JSON to Map");
	}

	@Test
	@DisplayName("fromJson()：非法 JSON 抛出 IllegalArgumentException")
	void fromJson_defaultMapper_rejectsInvalidJson() {
		assertThatThrownBy(() -> JsonSupport.fromJson("{bad", Map.class)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Failed to deserialize JSON to Map");
	}

}
