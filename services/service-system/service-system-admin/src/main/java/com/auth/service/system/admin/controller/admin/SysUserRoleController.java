package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.SysUserRoleService;
import com.auth.service.system.admin.service.authorization.query.UserAuthorizationSurfaceService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 用户角色授权
 *
 * @author Bunny
 */
@Tag(name = "用户角色授权", description = "用户直连角色分配")
@RequiredArgsConstructor
@RequestMapping("/api/system/user-role")
@RestController
public class SysUserRoleController {

	private final SysUserRoleService sysUserRoleService;

	private final UserAuthorizationSurfaceService userAuthorizationSurfaceService;

	@Operation(summary = "分页查询用户直连角色")
	@PreAuthorize("@auth.decide('sys:userrole:query')")
	@GetMapping("/{userId}/page")
	public Result<PageResponse<RoleReferenceVO>> pageRoles(@PathVariable("userId") Long userId,
			SubjectRolePageQuery query) {
		PageResponse<RoleReferenceVO> response = userAuthorizationSurfaceService.pageDirectRoles(userId, query);
		return Result.success(response);
	}

	@Operation(summary = "查询用户已分配角色", description = "Assign 灌种专用：全量返回；无授权为空数组")
	@PreAuthorize("@auth.decide('sys:userrole:query')")
	@GetMapping("/{userId}/roles")
	public Result<List<RoleReferenceVO>> listRoles(@PathVariable("userId") Long userId) {
		List<RoleReferenceVO> rows = sysUserRoleService.listAssignedRoles(userId);
		return Result.success(rows);
	}

	@OperationLog(targetType = "USER_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖分配用户角色")
	@PreAuthorize("@auth.decide('sys:userrole:update')")
	@PutMapping("/{userId}")
	public Result<Void> replaceRoles(@PathVariable("userId") Long userId,
			@Valid @RequestBody GrantTableAssignRoleForm form) {
		sysUserRoleService.replaceUserRoles(userId, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
