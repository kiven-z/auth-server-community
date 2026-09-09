package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 岗位扩展管理
 *
 * @author Bunny
 */
@Tag(name = "岗位扩展管理", description = "岗位角色分配")
@RequestMapping("/api/system/post")
@RestController
public class SysPostManageController {

	private final GrantTableService grantTableService;

	public SysPostManageController(GrantTableService grantTableService) {
		this.grantTableService = grantTableService;
	}

	@Operation(summary = "查询岗位已分配角色")
	@PreAuthorize("@auth.decide('sys:post:query')")
	@GetMapping("/{postId}/roles")
	public Result<List<RoleReferenceVO>> listRoles(@PathVariable("postId") Long postId) {
		List<RoleReferenceVO> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.POST, postId);
		return Result.success(rows);
	}

	@OperationLog(targetType = "POST_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖分配岗位角色", description = "全量覆盖；变更后触发授权失效")
	@PreAuthorize("@auth.decide('sys:post:update')")
	@PutMapping("/{postId}/roles")
	public Result<Void> replaceRoles(@PathVariable("postId") Long postId,
			@Valid @RequestBody GrantTableAssignRoleForm form) {
		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.POST, postId, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
