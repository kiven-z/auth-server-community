package com.auth.service.system.admin.controller.authorization;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.audit.operationlog.AuditServiceDomain;
import com.auth.module.security.contract.audit.operationlog.OperationLog;
import com.auth.module.security.contract.audit.operationlog.OperationLogKind;
import com.auth.module.security.contract.bizcode.PlatformBizCodes;
import com.auth.service.system.admin.model.query.authorization.RoleImpactUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.RoleImpactRefreshResultVO;
import com.auth.service.system.admin.model.vo.authorization.RoleImpactSummaryVO;
import com.auth.service.system.admin.model.vo.reference.UserReferenceVO;
import com.auth.service.system.admin.service.authorization.impact.RoleImpactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 角色影响面
 *
 * @author Bunny
 */
@Tag(name = "角色影响面", description = "影响摘要、影响用户分页、刷新受影响用户画像")
@RequiredArgsConstructor
@RequestMapping("/api/system/role")
@RestController
public class RoleImpactController {

	private final RoleImpactService roleImpactService;

	@Operation(summary = "查询角色影响面摘要")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/impact/summary")
	public Result<RoleImpactSummaryVO> impactSummary(@PathVariable("roleId") Long roleId) {
		RoleImpactSummaryVO summary = roleImpactService.getImpactSummary(roleId);
		return Result.success(summary);
	}

	@Operation(summary = "分页查询角色影响用户")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/impact/users/page")
	public Result<PageResponse<UserReferenceVO>> pageImpactUsers(@PathVariable("roleId") Long roleId,
			RoleImpactUserPageQuery query) {
		PageResponse<UserReferenceVO> response = roleImpactService.pageImpactUsers(roleId, query);
		return Result.success(response);
	}

	@OperationLog(targetType = "AUTH_PROFILE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.AUTH_PROFILE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "刷新角色受影响用户授权画像", description = "body 为空刷新全部影响用户；指定 ID 时与影响面取交集")
	@PreAuthorize("@auth.decide('sys:role:update')")
	@PostMapping("/{roleId}/impact/refresh")
	public Result<RoleImpactRefreshResultVO> refreshImpactedProfiles(@PathVariable("roleId") Long roleId,
			@RequestBody(required = false) List<Long> userIds) {
		RoleImpactRefreshResultVO result = roleImpactService.refreshImpactedProfiles(roleId, userIds);
		return Result.success(result, OPERATION_SUCCESS.getMessage());
	}

}
