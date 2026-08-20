package com.auth.module.security.autoconfigure.boot.pipeline;

import com.auth.module.security.autoconfigure.pipeline.resolver.HandlerMethodResolver;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementResolver;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementRule;
import com.auth.module.security.autoconfigure.pipeline.rule.AuthenticatedApiRule;
import com.auth.module.security.autoconfigure.pipeline.rule.InternalApiRule;
import com.auth.module.security.autoconfigure.pipeline.rule.PublicApiRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

/**
 * 安全需求解析：将 HandlerMethod 解析为 SecurityRequirement
 *
 * @author Bunny
 */
@Configuration
class SecurityRequirementResolverConfiguration {

	@Bean
	HandlerMethodResolver handlerMethodResolver(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		return new HandlerMethodResolver(requestMappingHandlerMapping);
	}

	/**
	 * 自定义注解
	 */
	@Bean
	PublicApiRule publicApiRule() {
		return new PublicApiRule();
	}

	@Bean
	AuthenticatedApiRule authenticatedApiRule() {
		return new AuthenticatedApiRule();
	}

	@Bean
	InternalApiRule internalApiRule() {
		return new InternalApiRule();
	}

	/**
	 * 规则集合：按优先级参与 SecurityRequirement 推导
	 */
	@Bean
	List<SecurityRequirementRule> securityRequirementRules(PublicApiRule publicApiRule, InternalApiRule internalApiRule,
			AuthenticatedApiRule authenticatedApiRule) {
		return List.of(publicApiRule, internalApiRule, authenticatedApiRule);
	}

	@Bean
	SecurityRequirementResolver securityRequirementResolver(HandlerMethodResolver handlerMethodResolver,
			List<SecurityRequirementRule> securityRequirementRules) {
		return new SecurityRequirementResolver(handlerMethodResolver, securityRequirementRules);
	}

}
