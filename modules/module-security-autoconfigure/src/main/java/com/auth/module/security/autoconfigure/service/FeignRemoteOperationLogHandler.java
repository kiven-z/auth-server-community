package com.auth.module.security.autoconfigure.service;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.feign.SystemSecurityIngestFeignClient;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.spi.OperationLogHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import static com.auth.common.web.resttemplate.FeignUtil.isSuccess;

/**
 * 本地无 log_operation 访问能力时，通过 Feign 将操作日志投递至 service-system。
 *
 * @author Bunny
 */
@Slf4j
public class FeignRemoteOperationLogHandler implements OperationLogHandler {

	private final SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	public FeignRemoteOperationLogHandler(SystemSecurityIngestFeignClient systemSecurityIngestFeignClient) {
		this.systemSecurityIngestFeignClient = systemSecurityIngestFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void handle(OperationLogPayloadEvent payload) {
		OperationLogIngestRequest body = OperationLogIngestRequest.fromPayload(payload);
		try {
			Result<Void> result = systemSecurityIngestFeignClient.appendOperationLog(body);
			if (!isSuccess(result)) {
				log.error("Operation log Feign returned non-success: code={}, message={}, requestUri={}, module={}",
						result != null ? result.getCode() : null, result != null ? result.getMessage() : null,
						payload.getRequestUri(), payload.getModule());
			}
		}
		catch (RuntimeException ex) {
			log.error("Failed to submit operation log via Feign, requestUri={}, module={}", payload.getRequestUri(),
					payload.getModule(), ex);
		}
	}

}
