package com.auth.service.system.schedule.task.support;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link BuiltinJobParamsParser} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("内置任务 job_params 解析")
class BuiltinJobParamsParserTest {

	private BuiltinJobParamsParser parser;

	@BeforeEach
	void setUp() {
		parser = new BuiltinJobParamsParser(new ObjectMapper());
	}

	@Test
	@DisplayName("parse：合法 JSON 应反序列化")
	void parse_whenJsonValid_returnsParams() {
		SampleParams actual = parser.parse("{\"name\":\"demo\"}", SampleParams.class);

		assertThat(actual.getName()).isEqualTo("demo");
	}

	@Test
	@DisplayName("parse：HttpInvokeJobParams method 应反序列化为 HttpMethod")
	void parse_whenHttpInvokeParams_deserializesHttpMethod() {
		HttpInvokeJobParams actual = parser.parse("{\"method\":\"POST\",\"url\":\"https://example.com\"}",
				HttpInvokeJobParams.class);

		assertThat(actual.getMethod()).isEqualTo(HttpMethod.POST);
		assertThat(actual.getUrl()).isEqualTo("https://example.com");
	}

	@Test
	@DisplayName("parse：HttpInvokeJobParams body 应反序列化为 JSON 对象")
	void parse_whenHttpInvokeParams_deserializesJsonObjectBody() {
		HttpInvokeJobParams actual = parser.parse(
				"{\"method\":\"POST\",\"url\":\"https://example.com\",\"body\":{\"title\":\"foo\",\"userId\":1}}",
				HttpInvokeJobParams.class);

		assertThat(actual.getBody()).isNotNull();
		assertThat(actual.getBody().get("title").asText()).isEqualTo("foo");
		assertThat(actual.getBody().get("userId").asInt()).isEqualTo(1);
	}

	@Test
	@DisplayName("parse(context)：应从 JobDataMap 读取并解析 jobParams")
	void parse_fromContext_readsRawJsonFromJobDataMap() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(SysJobQuartzDataKeys.JOB_PARAMS, "{\"name\":\"from-map\"}");
		when(context.getMergedJobDataMap()).thenReturn(jobDataMap);

		SampleParams actual = parser.parse(context, SampleParams.class);

		assertThat(actual.getName()).isEqualTo("from-map");
	}

	@Test
	@DisplayName("parse(context)：job_params 为空时应抛出 SysJobException")
	void parse_fromContext_whenMissing_throws() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

		assertThatThrownBy(() -> parser.parse(context, SampleParams.class)).isInstanceOf(SysJobException.class);
	}

	private static class SampleParams {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

}
