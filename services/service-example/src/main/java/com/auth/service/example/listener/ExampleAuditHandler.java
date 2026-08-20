package com.auth.service.example.listener;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 认证审计处理器
 *
 * @author Bunny
 */
@Slf4j
@Component
public class ExampleAuditHandler implements AuthorizationAuditHandler {

	private final ObjectMapper objectMapper;

	public ExampleAuditHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	@Async
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		log.info("======================================");
		try {
			log.info("{}", objectMapper.writeValueAsString(event));
		}
		catch (JsonProcessingException ex) {
			log.warn("Failed to serialize audit event", ex);
		}
		log.info("======================================");
	}

}