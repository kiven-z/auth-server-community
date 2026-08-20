package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.authorization.service.UserSessionRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * 用户会话撤销触发：事务提交后调用 auth 踢出全部会话
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserSessionRevocationTrigger {

	private final UserSessionRevocationService userSessionRevocationService;

	/**
	 * 在事务提交后批量踢出用户全部会话；无活跃事务时立即执行
	 * @param userIds 用户 ID 列表
	 */
	public void revokeAllSessionsAfterCommit(List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}

		List<Long> ids = List.copyOf(userIds);
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			userSessionRevocationService.revokeAllSessions(ids);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				userSessionRevocationService.revokeAllSessions(ids);
			}

		});
	}

}
