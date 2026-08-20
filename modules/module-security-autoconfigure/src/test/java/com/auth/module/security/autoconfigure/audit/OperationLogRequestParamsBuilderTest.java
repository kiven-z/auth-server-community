package com.auth.module.security.autoconfigure.audit;

import com.auth.module.security.autoconfigure.audit.operationlog.OperationLogRequestParamsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link OperationLogRequestParamsBuilder} 单元测试
 *
 * @author Bunny
 */
@DisplayName("OperationLogRequestParamsBuilder")
class OperationLogRequestParamsBuilderTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("record 为 false 时应返回 null")
	void build_shouldReturnNullWhenRecordDisabled() {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a");
		req.setQueryString("k=v");
		assertNull(OperationLogRequestParamsBuilder.build(req, false, objectMapper, null));
	}

	@Test
	@DisplayName("GET 应使用 queryString 并截断超长")
	void build_shouldUseQueryStringForGet() {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a");
		req.setQueryString("a".repeat(OperationLogRequestParamsBuilder.MAX_REQUEST_TEXT + 50));
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper, null);
		assertEquals(OperationLogRequestParamsBuilder.MAX_REQUEST_TEXT, out.length());
	}

	@Test
	@DisplayName("非 GET 应序列化 parameterMap")
	void build_shouldSerializeParameterMapForNonGet() {
		MockHttpServletRequest req = new MockHttpServletRequest("POST", "/a");
		req.addParameter("id", "1");
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper, null);
		assertTrue(out.contains("id"));
		assertTrue(out.contains("1"));
	}

	@Test
	@DisplayName("POST 无 parameterMap 时应从方法实参记录 JSON 绑定体")
	void build_shouldIncludeRequestBodyFromMethodArgs() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("POST", "/a");
		Map<String, Object> body = new HashMap<>();
		body.put("person", "(,,´•ω•)ノ(´っω•｀。)");
		body.put("kind", "😄");
		// 模拟 @RequestBody 绑定完成后的 Map，与 Servlet parameterMap 无关
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper, List.of(body));
		assertNotNull(out);
		JsonNode root = objectMapper.readTree(out);
		assertTrue(root.has("methodArgs"));
		assertTrue(root.get("methodArgs").get(0).get("kind").asText().contains("😄"));
	}

	@Test
	@DisplayName("GET 带 query 与方法实参时应合并为 JSON")
	void build_shouldMergeQueryStringAndMethodArgsForGet() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a");
		req.setQueryString("page=1");
		// 存在可记录实参时 GET 也改为结构化 JSON，便于同时携带 query 与 body 类参数
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper, List.of(99L));
		JsonNode root = objectMapper.readTree(out);
		assertEquals("page=1", root.get("queryString").asText());
		assertEquals(99L, root.get("methodArgs").get(0).asLong());
	}

	@Test
	@DisplayName("应跳过 HttpServletRequest 等框架占位参数")
	void build_shouldSkipServletRequestArgument() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("POST", "/a");
		Map<String, Object> body = Map.of("k", "v");
		// 第一个参数为请求对象时应忽略，避免与外层 request 重复
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper, List.of(req, body));
		JsonNode root = objectMapper.readTree(out);
		assertEquals(1, root.get("methodArgs").size());
		assertEquals("v", root.get("methodArgs").get(0).get("k").asText());
	}

	@Test
	@DisplayName("MultipartFile 应只记录元数据")
	void build_shouldSummarizeMultipartFile() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("POST", "/a");
		MockMultipartFile file = new MockMultipartFile("f", "a.txt", "text/plain", "x".getBytes());
		// 上传场景不落库二进制，仅文件名/大小/类型
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper, List.of(file));
		JsonNode root = objectMapper.readTree(out);
		JsonNode meta = root.get("methodArgs").get(0);
		assertEquals("MultipartFile", meta.get("_type").asText());
		assertEquals("a.txt", meta.get("originalFilename").asText());
		assertEquals(1L, meta.get("size").asLong());
	}

}
