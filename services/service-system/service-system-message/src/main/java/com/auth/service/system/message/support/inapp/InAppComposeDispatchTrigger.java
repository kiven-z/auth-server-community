package com.auth.service.system.message.support.inapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executor;

/**
 * 站内信群发派发触发
 *
 * @author Bunny
 */
@Slf4j
@Component
public class InAppComposeDispatchTrigger {

	private final InAppComposeDispatcher dispatcher;

	private final Executor inAppComposeExecutor;

	public InAppComposeDispatchTrigger(InAppComposeDispatcher dispatcher,
			@Qualifier("inAppComposeExecutor") Executor inAppComposeExecutor) {
		this.dispatcher = dispatcher;
		this.inAppComposeExecutor = inAppComposeExecutor;
	}

	/**
	 * 在事务提交后派发任务；无事务时立即派发
	 * @param taskId 任务 ID
	 */
	public void dispatchAfterCommit(Long taskId) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					dispatch(taskId);
				}
			});
			return;
		}
		dispatch(taskId);
	}

	/**
	 * 异步派发站内信任务
	 * @param taskId 任务 ID
	 */
	private void dispatch(Long taskId) {
		inAppComposeExecutor.execute(() -> {
			try {
				dispatcher.execute(taskId);
			}
			catch (RuntimeException ex) {
				log.error("In-app compose dispatch aborted, taskId={}", taskId, ex);
			}
		});
	}

}
