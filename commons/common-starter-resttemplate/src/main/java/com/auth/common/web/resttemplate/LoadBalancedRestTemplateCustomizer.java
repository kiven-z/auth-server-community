package com.auth.common.web.resttemplate;

import org.springframework.web.client.RestTemplate;

/**
 * 负载均衡 {@link RestTemplate} 创建时的扩展点，供 security 等模块注入出站拦截器
 *
 * @author Bunny
 */
@FunctionalInterface
public interface LoadBalancedRestTemplateCustomizer {

	/**
	 * 定制负载均衡 {@link RestTemplate}
	 * @param restTemplate 待定制的客户端
	 */
	void customize(RestTemplate restTemplate);

}
