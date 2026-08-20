package com.auth.module.security.autoconfigure.boot.pipeline;

import com.auth.module.security.autoconfigure.pipeline.authenticate.ExternalRequestAuthenticator;
import com.auth.module.security.autoconfigure.pipeline.authenticate.InternalRequestAuthenticator;
import com.auth.module.security.autoconfigure.pipeline.authenticate.RequestAuthenticator;
import com.auth.module.security.autoconfigure.pipeline.authenticate.SessionCountChecker;
import com.auth.module.security.autoconfigure.service.AuthProfileCacheService;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 请求认证器装配：内部/外部请求的认证入口
 *
 * @author Bunny
 */
@Configuration
class RequestAuthenticatorConfiguration {

	@Bean
	InternalRequestAuthenticator internalRequestAuthenticator(InternalTokenProvider internalTokenProvider,
			AuthProfileCacheService authProfileCacheService) {
		return new InternalRequestAuthenticator(internalTokenProvider, authProfileCacheService);
	}

	@Bean
	ExternalRequestAuthenticator externalRequestAuthenticator(AccessTokenProvider accessTokenProvider,
			AuthProfileCacheService authProfileCacheService, SessionCountChecker sessionCountChecker) {
		return new ExternalRequestAuthenticator(accessTokenProvider, authProfileCacheService, sessionCountChecker);
	}

	/**
	 * 认证器集合：按顺序尝试匹配请求来源并完成认证
	 */
	@Bean
	List<RequestAuthenticator> requestAuthenticators(InternalRequestAuthenticator internalRequestAuthenticator,
			ExternalRequestAuthenticator externalRequestAuthenticator) {
		return List.of(internalRequestAuthenticator, externalRequestAuthenticator);
	}

}
