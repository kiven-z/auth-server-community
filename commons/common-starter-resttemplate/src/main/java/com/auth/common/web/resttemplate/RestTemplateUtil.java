package com.auth.common.web.resttemplate;

import lombok.experimental.UtilityClass;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * {@link RestTemplate} 调用辅助
 *
 * @author Bunny
 */
@UtilityClass
public class RestTemplateUtil {

	/**
	 * 按 HTTP 方法发起调用，响应体按字符串解析
	 *
	 * <p>
	 * 未显式指定 Accept 时默认 application/json，避免内容协商落到 XML。
	 * </p>
	 * @param restTemplate HTTP 客户端
	 * @param url 请求 URL
	 * @param method HTTP 方法
	 * @param headers 可选请求头
	 * @param body 可选请求体
	 * @return 响应
	 */
	public static ResponseEntity<String> exchange(@NonNull RestTemplate restTemplate, @NonNull String url,
			@NonNull HttpMethod method, @Nullable Map<String, String> headers, @Nullable String body) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if (headers != null) {
			headers.forEach(httpHeaders::set);
		}
		if (!httpHeaders.containsKey(HttpHeaders.ACCEPT)) {
			httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
		}
		return restTemplate.exchange(url, method, new HttpEntity<>(body, httpHeaders), String.class);
	}

}
