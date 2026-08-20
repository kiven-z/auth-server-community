package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.authorization.dispatch.support.AuthorizationInvalidationSourceBizIds;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 角色码或启用状态变更、删除及权限分配后触发授权失效
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RoleAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按角色码提交失效事件（去重、忽略空白）
	 * @param roleCodes 角色编码，可含 old+new
	 * @param operation 操作动词，如 update、delete、assign-permissions
	 */
	public void submitByRoleCodes(Collection<String> roleCodes, String operation) {
		List<String> codes = roleCodes.stream().distinct().toList();
		if (CollUtil.isEmpty(codes)) {
			return;
		}

		String sourceBizId = AuthorizationInvalidationSourceBizIds.of(operation);
		String eventId = AuthorizationChangeKind.ROLE.eventIdPrefix() + ":" + IdWorker.getId();
		var payload = new RoleInvalidatePayload(codes);
		var request = new AuthorizationInvalidateRequest(eventId, AuthorizationChangeKind.ROLE, payload);

		invalidationCoordinator.submit(request, PlatformBizCodes.SYS_ROLE, sourceBizId);
	}

}
