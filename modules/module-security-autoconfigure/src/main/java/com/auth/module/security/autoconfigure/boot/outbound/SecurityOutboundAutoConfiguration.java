package com.auth.module.security.autoconfigure.boot.outbound;

import com.auth.common.web.resttemplate.CommonRestTemplateAutoConfiguration;
import com.auth.common.web.resttemplate.LoadBalancedRestTemplateCustomizer;
import com.auth.module.security.autoconfigure.outbound.InternalJwtClientHttpRequestInterceptor;
import com.auth.module.security.autoconfigure.outbound.InternalJwtFeignRequestInterceptor;
import com.auth.module.security.autoconfigure.outbound.OutboundInternalJwtIssuer;
import com.auth.module.security.core.autoconfigure.ModuleSecurityCore;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 微服务出站内部 JWT：Feign 与负载均衡 RestTemplate 共用签发逻辑
 *
 * @author Bunny
 */
@AutoConfiguration(after = ModuleSecurityCore.class, before = CommonRestTemplateAutoConfiguration.class)
public class SecurityOutboundAutoConfiguration {

	/**
	 * 出站内部 JWT 签发器
	 * @param internalTokenProvider 内部令牌提供者
	 * @param serviceId 当前服务名
	 * @return 签发器实例
	 */
	@ConditionalOnMissingBean
	@Bean
	OutboundInternalJwtIssuer outboundInternalJwtIssuer(InternalTokenProvider internalTokenProvider,
			@Value("${spring.application.name:unknown-service}") String serviceId) {
		return new OutboundInternalJwtIssuer(internalTokenProvider, serviceId);
	}

	/**
	 * Feign 出站拦截器
	 * @param jwtIssuer 出站内部 JWT 签发器
	 * @return Feign 拦截器
	 */
	@ConditionalOnClass(name = "feign.RequestInterceptor")
	@ConditionalOnMissingBean
	@Bean
	InternalJwtFeignRequestInterceptor internalJwtFeignRequestInterceptor(OutboundInternalJwtIssuer jwtIssuer) {
		return new InternalJwtFeignRequestInterceptor(jwtIssuer);
	}

	/**
	 * 负载均衡 RestTemplate 出站拦截器
	 * @param jwtIssuer 出站内部 JWT 签发器
	 * @return RestTemplate 拦截器
	 */
	@ConditionalOnClass(RestTemplate.class)
	@ConditionalOnMissingBean
	@Bean
	InternalJwtClientHttpRequestInterceptor internalJwtClientHttpRequestInterceptor(
			OutboundInternalJwtIssuer jwtIssuer) {
		return new InternalJwtClientHttpRequestInterceptor(jwtIssuer);
	}

	/**
	 * 在 loadBalancedRestTemplate 创建时挂载内部 JWT 拦截器
	 * @param interceptor 内部 JWT 拦截器
	 * @return 负载均衡 RestTemplate 定制器
	 */
	@ConditionalOnClass({ RestTemplate.class, LoadBalancedRestTemplateCustomizer.class })
	@ConditionalOnMissingBean(name = "internalJwtLoadBalancedRestTemplateCustomizer")
	@Bean
	LoadBalancedRestTemplateCustomizer internalJwtLoadBalancedRestTemplateCustomizer(
			InternalJwtClientHttpRequestInterceptor interceptor) {
		return restTemplate -> {
			List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
			boolean alreadyPresent = interceptors.stream()
				.anyMatch(InternalJwtClientHttpRequestInterceptor.class::isInstance);
			if (!alreadyPresent) {
				interceptors.add(interceptor);
				restTemplate.setInterceptors(interceptors);
			}
		};
	}

}
