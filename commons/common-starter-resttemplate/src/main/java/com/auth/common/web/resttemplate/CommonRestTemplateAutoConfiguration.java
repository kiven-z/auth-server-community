package com.auth.common.web.resttemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 自动配置：出站 HTTP 固定连接/读取超时，避免无超时拖死调用方线程。
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
public class CommonRestTemplateAutoConfiguration implements ApplicationRunner {

	/**
	 * 连接超时
	 */
	static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);

	/**
	 * 读取超时
	 */
	static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

	/**
	 * 创建带固定超时的 RestTemplate
	 * @return RestTemplate
	 */
	static RestTemplate createRestTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECT_TIMEOUT);
		factory.setReadTimeout(READ_TIMEOUT);
		return new RestTemplate(factory);
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Common RestTemplate initialized successfully");
	}

	/**
	 * 普通 RestTemplate
	 * <p>
	 * 用于调用外部 API（如邮件、地图、第三方接口）
	 * </p>
	 */
	@ConditionalOnMissingBean(name = "restTemplate")
	@Bean
	@Primary
	public RestTemplate restTemplate() {
		return createRestTemplate();
	}

	/**
	 * 负载均衡 RestTemplate
	 * <p>
	 * 用于微服务间调用，须配合 {@link LoadBalanced}
	 * </p>
	 */
	@ConditionalOnMissingBean(name = "loadBalancedRestTemplate")
	@LoadBalanced
	@Bean
	public RestTemplate loadBalancedRestTemplate(ObjectProvider<LoadBalancedRestTemplateCustomizer> customizers) {
		RestTemplate restTemplate = createRestTemplate();
		customizers.orderedStream().forEach(customizer -> customizer.customize(restTemplate));
		return restTemplate;
	}

}
