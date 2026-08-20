package com.auth.module.security.autoconfigure.boot.pipeline;

import com.auth.module.security.autoconfigure.pipeline.authenticate.SecurityAuthExecutor;
import com.auth.module.security.autoconfigure.pipeline.filter.TokenAuthenticationFilter;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementResolver;
import com.auth.module.security.autoconfigure.security.AuthAction;
import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 过滤器入口：注解需求分派并委托 SecurityAuthExecutor 认证
 *
 * @author Bunny
 */
@Configuration
class TokenAuthenticationFilterConfiguration {

	/**
	 * 需求 -> 动作分派表（默认映射，可被业务侧覆盖）
	 */
	@ConditionalOnMissingBean(name = "authActionDispatch")
	@Bean
	Map<SecurityRequirement, AuthAction> authActionDispatch() {
		return AuthAction.defaultDispatch();
	}

	/**
	 * 资源服务器认证过滤器：注解优先分派 + 委托认证执行器
	 */
	@Bean
	TokenAuthenticationFilter tokenAuthenticationFilter(SecurityRequirementResolver securityRequirementResolver,
			SecurityAuthExecutor securityAuthExecutor, Map<SecurityRequirement, AuthAction> authActionDispatch) {
		return new TokenAuthenticationFilter(securityRequirementResolver, securityAuthExecutor, authActionDispatch);
	}

}
