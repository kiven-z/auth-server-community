package com.auth.module.security.autoconfigure.boot.pipeline;

import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.module.security.autoconfigure.pipeline.authenticate.SessionCountChecker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 会话策略：提供会话数量/并发限制能力
 *
 * @author Bunny
 */
@Configuration
class SessionPolicyConfiguration {

	@Bean
	SessionCountChecker sessionCountChecker(
			@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate,
			UserConfigProperties userConfigProperties) {
		return new SessionCountChecker(sessionRedisTemplate, userConfigProperties);
	}

}
