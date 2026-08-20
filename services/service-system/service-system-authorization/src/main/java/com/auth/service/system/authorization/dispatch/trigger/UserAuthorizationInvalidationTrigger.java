package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.authorization.dispatch.support.AuthorizationInvalidationSourceBizIds;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 用户相关变更后按 userId 直连触发授权失效（主档、user_scope 等）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按用户 ID 提交失效（去重、忽略空）
	 * @param userIds 用户主键
	 * @param operation 操作动词，如 update、delete
	 */
	public void submitByUserIds(Collection<Long> userIds, String operation) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		String sourceBizId = AuthorizationInvalidationSourceBizIds.of(operation);
		String eventId = AuthorizationChangeKind.USER.eventIdPrefix() + ":" + IdWorker.getId();
		var payload = new UserInvalidatePayload(ids);
		var request = new AuthorizationInvalidateRequest(eventId, AuthorizationChangeKind.USER, payload);

		invalidationCoordinator.submit(request, PlatformBizCodes.SYS_USER, sourceBizId);
	}

}
