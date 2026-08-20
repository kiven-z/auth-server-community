package com.auth.service.auth.audit;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.feign.SystemSecurityIngestFeignClient;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.auth.common.web.resttemplate.FeignUtil.isSuccess;

/**
 * 认证审计处理器：经 Feign 调用 system 将授权决策事件持久化
 *
 * @author Bunny
 */
@Slf4j
@Component
public class LoggingAuthorizationAuditHandler implements AuthorizationAuditHandler {

	private final SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	public LoggingAuthorizationAuditHandler(SystemSecurityIngestFeignClient systemSecurityIngestFeignClient) {
		this.systemSecurityIngestFeignClient = systemSecurityIngestFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		AuthorizationAuditIngestRequest body = AuthorizationAuditIngestRequest.fromEvent(event);
		try {
			Result<Void> result = systemSecurityIngestFeignClient.appendAuthorizationAudit(body);
			if (!isSuccess(result)) {
				log.error(
						"Authorization audit Feign returned non-success: code={}, message={}, requestUri={}, userId={}",
						result != null ? result.getCode() : null, result != null ? result.getMessage() : null,
						event.getRequestUri(), event.getUserId());
			}
		}
		catch (RuntimeException ex) {
			log.error("Failed to submit authorization audit via Feign, requestUri={}, userId={}", event.getRequestUri(),
					event.getUserId(), ex);
		}
	}

}
