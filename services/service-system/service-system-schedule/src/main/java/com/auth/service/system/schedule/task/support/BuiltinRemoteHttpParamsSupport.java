package com.auth.service.system.schedule.task.support;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * 内置远程 HTTP 任务 query/body 解析与 URL 拼接
 *
 * @author Bunny
 */
@UtilityClass
public class BuiltinRemoteHttpParamsSupport {

	/**
	 * 将 query 拼接到 URL
	 * @param url 原始 URL
	 * @param query 查询参数
	 * @return 拼接后的 URL
	 */
	public static String buildUrlWithQuery(String url, Map<String, String> query) {
		if (MapUtil.isEmpty(query)) {
			return url;
		}
		UrlBuilder builder = UrlBuilder.ofHttp(url);
		query.forEach(builder::addQuery);
		return builder.build();
	}

	/**
	 * 将 JSON 请求体序列化为 HTTP 原始文本
	 * @param body JSON 节点
	 * @param objectMapper JSON 序列化器
	 * @return 序列化后的文本；null 或 JSON null 时返回 null
	 */
	public static String serializeBody(JsonNode body, ObjectMapper objectMapper) throws JsonProcessingException {
		if (body == null || body.isNull()) {
			return null;
		}
		return objectMapper.writeValueAsString(body);
	}

}
