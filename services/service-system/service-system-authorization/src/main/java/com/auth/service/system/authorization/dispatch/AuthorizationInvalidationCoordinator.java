package com.auth.service.system.authorization.dispatch;

import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.outbox.AuthorizationInvalidationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 授权失效协调入口：同事务写 Outbox，提交后（可选）同步 Feign 投递
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthorizationInvalidationCoordinator {

	private final AuthorizationInvalidationPublisher publisher;

	private final ApplicationEventPublisher applicationEventPublisher;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 提交失效事件：在调用方事务内写 Outbox，提交后按配置同步投递 auth。
	 * @param request 失效请求
	 * @param sourceModule 触发模块
	 * @param sourceBizId 业务主键
	 */
	@Transactional(rollbackFor = Exception.class)
	public void submit(AuthorizationInvalidateRequest request, String sourceModule, String sourceBizId) {
		Long outboxId = publisher.enqueue(request, sourceModule, sourceBizId);

		// 事务提交后是否同步
		Boolean enabled = properties.getSyncDispatchEnabled();
		if (enabled != null && enabled) {
			AuthorizationInvalidationDispatchEvent event = AuthorizationInvalidationDispatchEvent.builder()
				.outboxId(outboxId)
				.build();
			applicationEventPublisher.publishEvent(event);
		}
	}

}
