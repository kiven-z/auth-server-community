package com.auth.service.system.admin.ingest;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 将授权决策审计事件落库
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthAuditHandler implements AuthorizationAuditHandler {

	private final SecurityIngestService securityIngestService;

	public AuthAuditHandler(SecurityIngestService securityIngestService) {
		this.securityIngestService = securityIngestService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		try {
			securityIngestService.append(event);
		}
		catch (RuntimeException ex) {
			log.error("Failed to persist authorization audit event", ex);
		}
	}

}
