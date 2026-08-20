package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.role.SysRolePermissionAssignForm;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.model.vo.role.SysRoleScopeVO;
import com.auth.service.system.admin.service.admin.SysRolePermissionService;
import com.auth.service.system.admin.service.admin.SysRoleScopeService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 角色授权
 *
 * @author Bunny
 */
@Tag(name = "角色授权", description = "功能权限、数据范围")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/role")
@RestController
public class SysRoleAuthorizationController {

	private final SysRolePermissionService sysRolePermissionService;

	private final SysRoleScopeService sysRoleScopeService;

	@Operation(summary = "查询角色已分配权限码")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/permissions")
	public Result<List<PermissionReferenceVO>> listPermissions(@PathVariable("roleId") Long roleId) {
		List<PermissionReferenceVO> data = sysRolePermissionService.listAssignedPermissions(roleId);
		return Result.success(data);
	}

	@OperationLog(targetType = "ROLE_PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_ROLE_PERMISSION, operation = OperationLogKind.UPDATE)
	@Operation(summary = "全量分配角色权限码", description = "覆盖写；变更后触发授权失效")
	@PreAuthorize("@auth.decide('sys:role:update')")
	@PostMapping("/{roleId}/permissions")
	public Result<Void> assignPermissions(@PathVariable("roleId") Long roleId,
			@Valid @RequestBody SysRolePermissionAssignForm form) {
		sysRolePermissionService.assignPermissions(roleId, form.getPermissionIds());
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@Operation(summary = "查询角色数据范围", description = "未配置时 data=null")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/scope")
	public Result<SysRoleScopeVO> getScope(@PathVariable("roleId") Long roleId) {
		SysRoleScopeVO roleScopeVO = sysRoleScopeService.getByRoleId(roleId);
		return Result.success(roleScopeVO);
	}

	@OperationLog(targetType = "ROLE_SCOPE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_ROLE_SCOPE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "保存角色数据范围", description = "有则改无则插；变更后触发授权失效")
	@PreAuthorize("@auth.decide('sys:role:update')")
	@PutMapping("/{roleId}/scope")
	public Result<Void> upsertScope(@PathVariable("roleId") Long roleId, @Valid @RequestBody SysDataScopeForm form) {
		sysRoleScopeService.upsert(roleId, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
