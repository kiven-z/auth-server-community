package com.auth.module.security.autoconfigure.service;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.spi.OperationLogHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 空操作日志处理器 — 当业务服务未提供 {@link OperationLogHandler} 实例时生效
 *
 * @author Bunny
 */
@Slf4j
public class NoopOperationLogHandler implements OperationLogHandler {

	/**
	 * 启动提示：未接入操作日志持久化
	 */
	@PostConstruct
	void logHint() {
		log.info(
				"OperationLogHandler: no implementation found. Operation log payloads will be assembled by AOP but not persisted. "
						+ "Register a bean implementing OperationLogHandler to write log_operation.");
	}

	@Override
	public void handle(OperationLogPayloadEvent payload) {
		// intentionally empty
	}

}
