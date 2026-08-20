package com.auth.service.system.schedule.task.builtin;

import cn.hutool.core.net.url.UrlBuilder;
import com.auth.common.web.resttemplate.RestTemplateUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.support.BuiltinRemoteHttpParamsSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 通用微服务远程调用内置任务：通过负载均衡 {@link RestTemplate} 访问 http://{serviceName{path}}。 出站
 * X-Internal-JWT 由 security 模块自动附加，无需在 job_params 中配置。
 *
 * @author Bunny
 */
@QuartzTask(name = "FeignInvoke",
		description = "微服务 HTTP：serviceName+path+method 必填；headers/query/body 为 JSON；JWT 自动附加",
		modes = { SysJobTaskType.CUSTOM_CLASS },
		example = """
				{"serviceName":"service-example","path":"/api/example/inner/schedule","method":"DELETE","headers":{"Content-Type":"application/json"},"query":{"id":1},"body":{"id":1}}
				""")
@Slf4j
@Component
public class FeignInvokeJob implements Job {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	private final RestTemplate loadBalancedRestTemplate;

	private final ObjectMapper objectMapper;

	public FeignInvokeJob(BuiltinJobParamsParser builtinJobParamsParser,
			@Qualifier("loadBalancedRestTemplate") RestTemplate loadBalancedRestTemplate, ObjectMapper objectMapper) {
		this.builtinJobParamsParser = builtinJobParamsParser;
		this.loadBalancedRestTemplate = loadBalancedRestTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			FeignInvokeJobParams params = builtinJobParamsParser.parse(context, FeignInvokeJobParams.class);

			String serviceName = params.getServiceName().trim();
			String path = params.getPath().trim();
			String baseUrl = UrlBuilder.of().setScheme("http").setHost(serviceName).addPath(path).build();
			String url = BuiltinRemoteHttpParamsSupport.buildUrlWithQuery(baseUrl, params.getQuery());

			HttpMethod method = HttpMethod.valueOf(params.getMethod().name());

			Map<String, String> headers = params.getHeaders();
			String body = BuiltinRemoteHttpParamsSupport.serializeBody(params.getBody(), objectMapper);
			ResponseEntity<String> response = RestTemplateUtil.exchange(loadBalancedRestTemplate, url, method, headers,
					body);

			String responseBody = response.getBody();
			log.debug("FeignInvokeJob ok service={} path={} method={} status={} respLen={}", params.getServiceName(),
					params.getPath(), params.getMethod(), response.getStatusCode().value(),
					responseBody == null ? 0 : responseBody.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
