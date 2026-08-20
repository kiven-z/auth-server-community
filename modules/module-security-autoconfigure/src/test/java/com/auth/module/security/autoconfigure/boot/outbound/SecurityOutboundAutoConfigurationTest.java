package com.auth.module.security.autoconfigure.boot.outbound;

import com.auth.common.jwt.autoconfigure.JwtAutoConfiguration;
import com.auth.common.web.resttemplate.CommonRestTemplateAutoConfiguration;
import com.auth.module.security.autoconfigure.outbound.InternalJwtClientHttpRequestInterceptor;
import com.auth.module.security.autoconfigure.outbound.InternalJwtFeignRequestInterceptor;
import com.auth.module.security.autoconfigure.outbound.OutboundInternalJwtIssuer;
import com.auth.module.security.core.autoconfigure.ModuleSecurityCore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SecurityOutboundAutoConfiguration} 自动配置测试。
 */
@DisplayName("SecurityOutboundAutoConfiguration 出站内部 JWT")
class SecurityOutboundAutoConfigurationTest {

	private static final String JWT_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab";

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(JwtAutoConfiguration.class, ModuleSecurityCore.class,
				SecurityOutboundAutoConfiguration.class, CommonRestTemplateAutoConfiguration.class))
		.withPropertyValues("auth.common.jwt.issuer=test-iss", "auth.common.jwt.secret=" + JWT_SECRET,
				"auth.common.jwt.access-expired=3600", "auth.common.jwt.refresh-expired=7200",
				"spring.application.name=service-system");

	@Test
	@DisplayName("自动配置：应注册出站 JWT 签发器与 Feign/RestTemplate 拦截器")
	void autoConfiguration_shouldRegisterOutboundJwtBeans() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(OutboundInternalJwtIssuer.class);
			assertThat(context).hasSingleBean(InternalJwtFeignRequestInterceptor.class);
			assertThat(context).hasSingleBean(InternalJwtClientHttpRequestInterceptor.class);
			assertThat(context).hasBean("internalJwtLoadBalancedRestTemplateCustomizer");
		});
	}

	@Test
	@DisplayName("自动配置：loadBalancedRestTemplate 应挂载内部 JWT 拦截器")
	void autoConfiguration_loadBalancedRestTemplateShouldAttachInternalJwtInterceptor() {
		contextRunner.run(context -> {
			RestTemplate restTemplate = context.getBean("loadBalancedRestTemplate", RestTemplate.class);
			assertThat(restTemplate.getInterceptors())
				.anyMatch(InternalJwtClientHttpRequestInterceptor.class::isInstance);
		});
	}

}
