package com.auth.service.system.schedule.task.builtin;

import com.auth.common.web.resttemplate.RestTemplateUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.support.BuiltinRemoteHttpParamsSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 通用 HTTP 内置任务：从 job_params 读取 method、url 与可选 headers/query/body 并发起调用
 *
 * @author Bunny
 */
@QuartzTask(name = "HttpInvoke", description = "外网 HTTP：method+url 必填；headers/query/body 为 JSON；query 仅一层键值",
		modes = { SysJobTaskType.CUSTOM_CLASS },
		example = """
				{"method":"POST","url":"https://example.com/api/items","headers":{"Content-Type":"application/json"},"query":{"page":"1","userId":2},"body":{"id":1}}
				""")
@RequiredArgsConstructor
@Slf4j
@Component
public class HttpInvokeJob implements Job {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			HttpInvokeJobParams params = builtinJobParamsParser.parse(context, HttpInvokeJobParams.class);

			String url = BuiltinRemoteHttpParamsSupport.buildUrlWithQuery(params.getUrl(), params.getQuery());
			HttpMethod method = params.getMethod();

			Map<String, String> headers = params.getHeaders();
			String body = BuiltinRemoteHttpParamsSupport.serializeBody(params.getBody(), objectMapper);
			ResponseEntity<String> response = RestTemplateUtil.exchange(restTemplate, url, method, headers, body);

			String responseBody = response.getBody();
			log.debug("HttpInvokeJob ok method={} url={} status={} respLen={}", method, url,
					response.getStatusCode().value(), responseBody == null ? 0 : responseBody.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
