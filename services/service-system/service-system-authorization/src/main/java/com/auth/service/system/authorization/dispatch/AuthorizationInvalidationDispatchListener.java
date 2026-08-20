package com.auth.service.system.authorization.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 业务事务提交后同步调用 auth 失效接口（过渡方案）。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationDispatchListener {

	private final AuthorizationInvalidationOutboxProcessor outboxProcessor;

	/**
	 * 事务提交后投递 Outbox。
	 * @param event 投递事件
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAfterCommit(AuthorizationInvalidationDispatchEvent event) {
		Long outboxId = event.outboxId();
		if (outboxId == null) {
			return;
		}

		outboxProcessor.processById(outboxId);
	}

}
