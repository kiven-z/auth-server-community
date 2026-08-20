package com.auth.common.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * 基于 {@link ObjectMapper} 的 JSON 辅助工具。
 *
 * <p>
 * 类内维护默认 ObjectMapper 供无参重载使用，其配置与 Spring HTTP 层的 ObjectMapper 并不完全一致；Spring 服务应优先传入注入的
 * ObjectMapper。
 *
 * @author Bunny
 */
@UtilityClass
public class JsonSupport {

	/**
	 * 无参重载使用的默认 {@link ObjectMapper}，仅包含基础序列化配置。
	 */
	private static final ObjectMapper DEFAULT_MAPPER = createDefaultMapper();

	private static ObjectMapper createDefaultMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		return mapper;
	}

	/**
	 * 将对象序列化为 JSON 字符串（使用默认 {@link ObjectMapper}）。
	 * @param value 待序列化的值
	 * @return JSON 字符串
	 */
	public static String toJson(Object value) {
		return toJson(DEFAULT_MAPPER, value);
	}

	/**
	 * 将 JSON 字符串反序列化为指定类型（使用默认 {@link ObjectMapper}）。
	 * @param json JSON 字符串
	 * @param type 目标类型
	 * @param <T> 目标类型
	 * @return 反序列化后的对象
	 */
	public static <T> T fromJson(String json, Class<T> type) {
		return fromJson(DEFAULT_MAPPER, json, type);
	}

	/**
	 * 将 JSON 字符串解析为 {@link JsonNode}（使用默认 {@link ObjectMapper}）。
	 * @param json JSON 字符串
	 * @return 解析后的节点
	 */
	public static JsonNode readTree(String json) {
		return readTree(DEFAULT_MAPPER, json);
	}

	/**
	 * 将 JSON 字符串解析为对象型 {@link JsonNode}（使用默认 {@link ObjectMapper}）。
	 * @param json JSON 字符串
	 * @return 对象节点
	 */
	public static JsonNode readObjectTree(String json) {
		return readObjectTree(DEFAULT_MAPPER, json);
	}

	/**
	 * 将对象序列化为 JSON 字符串。
	 * @param mapper ObjectMapper
	 * @param value 待序列化的值
	 * @return JSON 字符串
	 */
	public static String toJson(ObjectMapper mapper, Object value) {
		try {
			return mapper.writeValueAsString(value);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Failed to serialize value to JSON", ex);
		}
	}

	/**
	 * 将 JSON 字符串反序列化为指定类型。
	 * @param mapper ObjectMapper
	 * @param json JSON 字符串
	 * @param type 目标类型
	 * @param <T> 目标类型
	 * @return 反序列化后的对象
	 */
	public static <T> T fromJson(ObjectMapper mapper, String json, Class<T> type) {
		try {
			return mapper.readValue(json, type);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Failed to deserialize JSON to " + type.getSimpleName(), ex);
		}
	}

	/**
	 * 将 JSON 字符串反序列化为带单类型参数的泛型。
	 * @param json JSON 字符串
	 * @param rawType 原始类型
	 * @param typeParameter 类型参数
	 * @param <T> 目标类型
	 * @return 反序列化后的对象
	 */
	public static <T> T fromJson(String json, Class<?> rawType, Class<?> typeParameter) {
		JavaType javaType = DEFAULT_MAPPER.getTypeFactory().constructParametricType(rawType, typeParameter);
		try {
			return DEFAULT_MAPPER.readValue(json, javaType);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Failed to deserialize JSON to " + rawType.getSimpleName() + "<"
					+ typeParameter.getSimpleName() + ">", ex);
		}
	}

	/**
	 * 将 JSON 字符串解析为 {@link JsonNode}。
	 * @param mapper ObjectMapper
	 * @param json JSON 字符串
	 * @return 解析后的节点
	 */
	public static JsonNode readTree(ObjectMapper mapper, String json) {
		try {
			return mapper.readTree(json);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Failed to parse JSON", ex);
		}
	}

	/**
	 * 将 JSON 字符串解析为对象型 {@link JsonNode}。
	 * @param mapper ObjectMapper
	 * @param json JSON 字符串
	 * @return 对象节点
	 */
	public static JsonNode readObjectTree(ObjectMapper mapper, String json) {
		JsonNode node = readTree(mapper, json);
		if (!node.isObject()) {
			throw new IllegalArgumentException("JSON value must be an object");
		}
		return node;
	}

}
