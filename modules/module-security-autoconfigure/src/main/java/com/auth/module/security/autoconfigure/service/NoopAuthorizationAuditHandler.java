package com.auth.module.security.autoconfigure.service;

import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 空审计处理器 — 当业务服务未提供 {@link AuthorizationAuditHandler} 实例时生效
 *
 * <p>
 * 仅记录一条 info 日志告知部署者审计未接入，不执行任何持久化
 * </p>
 *
 * @author Bunny
 */
@Slf4j
public class NoopAuthorizationAuditHandler implements AuthorizationAuditHandler {

	@PostConstruct
	void logHint() {
		log.info(
				"AuthorizationAuditHandler: no implementation found. Audit events will be published via ApplicationEvent but not persisted. "
						+ "To enable audit persistence, register a bean implementing AuthorizationAuditHandler.");
	}

	@Override
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		// no-op: events are still published via ApplicationEventPublisher for loose
		// coupling
	}

}
