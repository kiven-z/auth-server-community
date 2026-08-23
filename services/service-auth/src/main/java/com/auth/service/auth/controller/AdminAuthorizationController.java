package com.auth.service.auth.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.service.auth.service.AdminAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端授权
 *
 * @author Bunny
 */
@Tag(name = "管理端授权", description = "管理员刷新用户授权画像缓存")
@RequiredArgsConstructor
@RequestMapping("/api/auth/admin/users")
@RestController
public class AdminAuthorizationController {

	private final AdminAuthorizationService adminAuthorizationService;

	@OperationLog(targetType = "AUTH_PROFILE", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = PlatformBizCodes.AUTH_PROFILE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量刷新用户授权画像", description = "刷新 AuthProfile 缓存")
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/authorization/refresh")
	public Result<Void> refreshAuthorization(@RequestBody(required = false) List<Long> userIds) {
		adminAuthorizationService.refreshUserCache(userIds);
		return Result.success();
	}

}
