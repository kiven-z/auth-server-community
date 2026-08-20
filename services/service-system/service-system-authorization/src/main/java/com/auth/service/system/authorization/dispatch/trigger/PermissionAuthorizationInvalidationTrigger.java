package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.PermissionInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.authorization.dispatch.support.AuthorizationInvalidationSourceBizIds;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 权限码或启用状态变更、删除后触发授权失效
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PermissionAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按权限码提交失效事件（去重、忽略空白）
	 * @param permissionCodes 权限码列表
	 * @param operation 操作动词，如 update、delete
	 */
	public void submitByPermissionCodes(Collection<String> permissionCodes, String operation) {
		List<String> codes = permissionCodes.stream().distinct().toList();
		if (CollUtil.isEmpty(codes)) {
			return;
		}

		String sourceBizId = AuthorizationInvalidationSourceBizIds.of(operation);
		String eventId = AuthorizationChangeKind.PERMISSION.eventIdPrefix() + ":" + IdWorker.getId();
		var payload = new PermissionInvalidatePayload(codes);
		var request = new AuthorizationInvalidateRequest(eventId, AuthorizationChangeKind.PERMISSION, payload);

		invalidationCoordinator.submit(request, PlatformBizCodes.SYS_PERMISSION, sourceBizId);
	}

}
