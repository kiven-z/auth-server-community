package com.auth.service.system.schedule.task.builtin;

import com.auth.service.system.schedule.model.jobparams.BuiltinRemoteHttpMethod;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
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
 * {@link FeignInvokeJob} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FeignInvokeJob 微服务远程调用内置任务")
@ExtendWith(MockitoExtension.class)
class FeignInvokeJobTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	@Mock
	private RestTemplate loadBalancedRestTemplate;

	private FeignInvokeJob feignInvokeJob;

	@BeforeEach
	void setUp() {
		feignInvokeJob = new FeignInvokeJob(builtinJobParamsParser, loadBalancedRestTemplate, objectMapper);
	}

	@Test
	@DisplayName("execute：serviceName 含首尾空格时应 trim 后拼接 URL")
	void execute_trimsServiceNameBeforeBuildingUrl() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("  auth-service  ");
		params.setPath("/api/health");
		params.setMethod(BuiltinRemoteHttpMethod.GET);
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://auth-service/api/health"), eq(HttpMethod.GET),
				ArgumentMatchers.any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("UP"));

		feignInvokeJob.execute(context);

		verify(loadBalancedRestTemplate).exchange(eq("http://auth-service/api/health"), eq(HttpMethod.GET),
				ArgumentMatchers.any(), eq(String.class));
	}

	@Test
	@DisplayName("execute：应使用负载均衡 URL 与 POST JSON body 发起调用")
	void execute_invokesPostAgainstLoadBalancedServiceUrl() throws Exception {
		JobExecutionContext context = mock(JobExecutionContext.class);

		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("order-service");
		params.setPath("/api/internal/sync");
		params.setMethod(BuiltinRemoteHttpMethod.POST);
		params.setHeaders(java.util.Map.of("Content-Type", "application/json"));
		params.setBody(objectMapper.readTree("{\"id\":1}"));
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://order-service/api/internal/sync"), eq(HttpMethod.POST),
				ArgumentMatchers.any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("ok"));

		feignInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(loadBalancedRestTemplate).exchange(eq("http://order-service/api/internal/sync"), eq(HttpMethod.POST),
				entityCaptor.capture(), eq(String.class));
		HttpEntity<String> request = entityCaptor.getValue();
		assertThat(request.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
		assertThat(request.getHeaders().getFirst("Accept")).isEqualTo("application/json");
		assertThat(request.getBody()).isEqualTo("{\"id\":1}");
	}

	@Test
	@DisplayName("execute：未配置 headers 时 RestTemplateUtil 应默认 Accept application/json")
	void execute_whenHeadersMissing_defaultsAcceptJson() throws Exception {
		JobExecutionContext context = mock(JobExecutionContext.class);

		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("order-service");
		params.setPath("/api/internal/ping");
		params.setMethod(BuiltinRemoteHttpMethod.GET);
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://order-service/api/internal/ping"), eq(HttpMethod.GET),
				ArgumentMatchers.any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("ok"));

		feignInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(loadBalancedRestTemplate).exchange(eq("http://order-service/api/internal/ping"), eq(HttpMethod.GET),
				entityCaptor.capture(), eq(String.class));
		assertThat(entityCaptor.getValue().getHeaders().getFirst("Accept")).isEqualTo("application/json");
	}

	@Test
	@DisplayName("execute：GET 应拼接 query 且不携带 body")
	void execute_getRequest_appendsQueryWithoutBody() throws Exception {
		JobExecutionContext context = mock(JobExecutionContext.class);

		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("auth-service");
		params.setPath("/api/health");
		params.setMethod(BuiltinRemoteHttpMethod.GET);
		params.setQuery(java.util.Map.of("verbose", "true"));
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(org.mockito.ArgumentMatchers.contains("verbose=true"),
				eq(HttpMethod.GET), ArgumentMatchers.any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("UP"));

		feignInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(loadBalancedRestTemplate).exchange(org.mockito.ArgumentMatchers.contains("verbose=true"),
				eq(HttpMethod.GET), entityCaptor.capture(), eq(String.class));
		assertThat(entityCaptor.getValue().getBody()).isNull();
	}

	@Test
	@DisplayName("execute：RestTemplate 异常应包装为 JobExecutionException")
	void execute_whenRestTemplateFails_wrapsJobExecutionException() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("order-service");
		params.setPath("/api/fail");
		params.setMethod(BuiltinRemoteHttpMethod.DELETE);
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://order-service/api/fail"), eq(HttpMethod.DELETE),
				ArgumentMatchers.any(), eq(String.class)))
			.thenThrow(new RuntimeException("connection refused"));

		assertThatThrownBy(() -> feignInvokeJob.execute(context)).isInstanceOf(JobExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);
	}

	private void stubJobParams(JobExecutionContext context, FeignInvokeJobParams params) {
		when(builtinJobParamsParser.parse(same(context), eq(FeignInvokeJobParams.class))).thenReturn(params);
	}

}
