package com.auth.service.system.schedule.task.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BuiltinRemoteHttpParamsSupport} 单元测试。
 */
@DisplayName("BuiltinRemoteHttpParamsSupport 远程 HTTP 参数工具")
class BuiltinRemoteHttpParamsSupportTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("buildUrlWithQuery：应拼接 query 到 URL")
	void buildUrlWithQuery_appendsQueryParams() {
		String actual = BuiltinRemoteHttpParamsSupport.buildUrlWithQuery("https://example.com/api",
				Map.of("page", "1", "userId", "2"));

		assertThat(actual).contains("page=1").contains("userId=2");
	}

	@Test
	@DisplayName("buildUrlWithQuery：空 query 应返回原 URL")
	void buildUrlWithQuery_whenQueryEmpty_returnsOriginalUrl() {
		String actual = BuiltinRemoteHttpParamsSupport.buildUrlWithQuery("https://example.com/api", null);

		assertThat(actual).isEqualTo("https://example.com/api");
	}

	@Test
	@DisplayName("serializeBody：JSON 对象应序列化为 HTTP 文本")
	void serializeBody_whenObject_returnsJsonText() throws Exception {
		var body = objectMapper.readTree("{\"id\":1}");

		String actual = BuiltinRemoteHttpParamsSupport.serializeBody(body, objectMapper);

		assertThat(actual).isEqualTo("{\"id\":1}");
	}

}
