package com.auth.module.security.autoconfigure.boot.validation;

import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.module.security.autoconfigure.annotation.processor.MutuallyExclusiveAnnotationsRule;
import com.auth.module.security.autoconfigure.annotation.processor.SecurityAnnotationConflictBeanPostProcessor;
import com.auth.module.security.autoconfigure.annotation.processor.SecurityAnnotationConflictRule;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import java.util.List;
import java.util.Set;

/**
 * 启动期安全注解冲突校验自动配置：在 Controller 类/方法级别发现互斥注解组合时 fail-fast
 *
 * @author Bunny
 */
@Configuration(proxyBeanMethods = false)
public class SecurityAnnotationConflictAutoConfiguration {

	private SecurityAnnotationConflictAutoConfiguration() {

	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static MutuallyExclusiveAnnotationsRule securityApiVsMethodSecurityRule() {
		return new MutuallyExclusiveAnnotationsRule("securityApiVsMethodSecurity",
				Set.of(PublicApi.class, AuthenticatedApi.class, InternalApi.class, PreAuthorize.class,
						PostAuthorize.class, PreFilter.class, PostFilter.class));
	}

	@ConditionalOnMissingBean
	@Bean
	public static SecurityAnnotationConflictBeanPostProcessor securityAnnotationConflictBeanPostProcessor(
			List<SecurityAnnotationConflictRule> conflictRules) {
		return new SecurityAnnotationConflictBeanPostProcessor(conflictRules);
	}

}