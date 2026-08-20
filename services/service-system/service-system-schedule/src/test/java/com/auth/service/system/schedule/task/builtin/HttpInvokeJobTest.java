package com.auth.service.system.schedule.task.builtin;

import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link HttpInvokeJob} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("HttpInvokeJob HTTP 内置任务")
@ExtendWith(MockitoExtension.class)
class HttpInvokeJobTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	@Mock
	private RestTemplate restTemplate;

	private HttpInvokeJob httpInvokeJob;

	@BeforeEach
	void setUp() {
		httpInvokeJob = new HttpInvokeJob(builtinJobParamsParser, restTemplate, objectMapper);
	}

	@Test
	@DisplayName("execute：应使用 job_params 中的 url、query 与 headers 发起 GET")
	void execute_invokesGetWithConfiguredUrlQueryAndHeaders() throws Exception {
		JobExecutionContext context = mock(JobExecutionContext.class);

		HttpInvokeJobParams params = new HttpInvokeJobParams();
		params.setMethod(HttpMethod.GET);
		params.setUrl("https://example.com/api");
		params.setQuery(java.util.Map.of("page", "1"));
		params.setHeaders(java.util.Map.of("X-Token", "abc"));
		stubJobParams(context, params);
		when(restTemplate.exchange(org.mockito.ArgumentMatchers.contains("https://example.com/api"), eq(HttpMethod.GET),
				org.mockito.ArgumentMatchers.<HttpEntity<Void>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("ok"));

		httpInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(org.mockito.ArgumentMatchers.contains("page=1"), eq(HttpMethod.GET),
				entityCaptor.capture(), eq(String.class));
		assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Token")).isEqualTo("abc");
	}

	@Test
	@DisplayName("execute：POST 应携带 JSON 对象 body 发起调用")
	void execute_invokesPostWithJsonObjectBody() throws Exception {
		JobExecutionContext context = mock(JobExecutionContext.class);

		HttpInvokeJobParams params = new HttpInvokeJobParams();
		params.setMethod(HttpMethod.POST);
		params.setUrl("https://example.com/api");
		params.setBody(objectMapper.readTree("{\"id\":1}"));
		stubJobParams(context, params);
		when(restTemplate.exchange(eq("https://example.com/api"), eq(HttpMethod.POST),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("ok"));

		httpInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api"), eq(HttpMethod.POST), entityCaptor.capture(),
				eq(String.class));
		assertThat(entityCaptor.getValue().getBody()).isEqualTo("{\"id\":1}");
	}

	@Test
	@DisplayName("execute：RestTemplate 异常应包装为 JobExecutionException")
	void execute_whenRestTemplateFails_wrapsJobExecutionException() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		HttpInvokeJobParams params = new HttpInvokeJobParams();
		params.setMethod(HttpMethod.GET);
		params.setUrl("https://example.com/fail");
		stubJobParams(context, params);
		when(restTemplate.exchange(eq("https://example.com/fail"), eq(HttpMethod.GET),
				org.mockito.ArgumentMatchers.<HttpEntity<Void>>any(), eq(String.class)))
			.thenThrow(new RuntimeException("timeout"));

		assertThatThrownBy(() -> httpInvokeJob.execute(context)).isInstanceOf(JobExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);
	}

	private void stubJobParams(JobExecutionContext context, HttpInvokeJobParams params) {
		when(builtinJobParamsParser.parse(same(context), eq(HttpInvokeJobParams.class))).thenReturn(params);
	}

}
