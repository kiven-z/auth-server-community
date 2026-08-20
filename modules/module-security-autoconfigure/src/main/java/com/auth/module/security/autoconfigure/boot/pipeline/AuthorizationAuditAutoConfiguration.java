package com.auth.module.security.autoconfigure.boot.pipeline;

import com.auth.module.security.autoconfigure.service.NoopAuthorizationAuditHandler;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 审计兜底：未提供 AuthorizationAuditHandler 时启用空实现
 *
 * @author Bunny
 */
@Configuration
class AuthorizationAuditAutoConfiguration {

	/**
	 * 空审计处理器（业务服务未提供 SPI 实现时生效）
	 */
	@ConditionalOnMissingBean(AuthorizationAuditHandler.class)
	@Bean
	NoopAuthorizationAuditHandler noopAuthorizationAuditHandler() {
		return new NoopAuthorizationAuditHandler();
	}

}
