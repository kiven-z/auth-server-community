package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.GrantInvalidatePayload;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.authorization.dispatch.support.AuthorizationInvalidationSourceBizIds;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * grant_table 授权主体变更后触发用户授权画像失效
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class GrantAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按授权主体提交失效事件
	 * @param subjects 授权主体键列表
	 * @param operation 操作标识
	 */
	public void submitByGrantSubjects(Collection<GrantSubjectKey> subjects, String operation) {
		List<GrantSubjectKey> keys = subjects.stream().distinct().toList();
		if (CollUtil.isEmpty(keys)) {
			return;
		}

		String sourceBizId = AuthorizationInvalidationSourceBizIds.of(operation);
		String eventId = AuthorizationChangeKind.GRANT.eventIdPrefix() + ":" + IdWorker.getId();
		var payload = new GrantInvalidatePayload(keys);
		var request = new AuthorizationInvalidateRequest(eventId, AuthorizationChangeKind.GRANT, payload);

		invalidationCoordinator.submit(request, AuthorizationAuditBizModule.GRANT_TABLE, sourceBizId);
	}

}
