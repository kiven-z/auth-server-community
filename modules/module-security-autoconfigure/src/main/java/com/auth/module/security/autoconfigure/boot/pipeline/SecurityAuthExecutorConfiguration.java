package com.auth.module.security.autoconfigure.boot.pipeline;

import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.autoconfigure.pipeline.authenticate.InternalRequestAuthenticator;
import com.auth.module.security.autoconfigure.pipeline.authenticate.RequestAuthenticator;
import com.auth.module.security.autoconfigure.pipeline.authenticate.SecurityAuthExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 认证执行：协调认证器并写入 SecurityContext
 *
 * @author Bunny
 */
@Configuration
class SecurityAuthExecutorConfiguration {

	@Bean
	AuthProfileSecurityContextPopulator authProfileSecurityContextPopulator() {
		return new AuthProfileSecurityContextPopulator();
	}

	/**
	 * 认证执行器：统一编排认证与上下文写入
	 */
	@Bean
	SecurityAuthExecutor securityAuthExecutor(InternalRequestAuthenticator internalRequestAuthenticator,
			List<RequestAuthenticator> requestAuthenticators,
			AuthProfileSecurityContextPopulator authProfileSecurityContextPopulator) {
		return new SecurityAuthExecutor(internalRequestAuthenticator, requestAuthenticators,
				authProfileSecurityContextPopulator);
	}

}
