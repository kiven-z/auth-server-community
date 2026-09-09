package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.admin.support.dept.DeptClosureHealthInspector;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 部门扩展管理
 *
 * @author Bunny
 */
@Tag(name = "部门扩展管理", description = "闭包健康检查、部门角色分配")
@RequestMapping("/api/system/dept")
@RestController
public class SysDeptManageController {

	private final GrantTableService grantTableService;

	private final DeptClosureHealthInspector deptClosureHealthInspector;

	public SysDeptManageController(GrantTableService grantTableService,
			DeptClosureHealthInspector deptClosureHealthInspector) {
		this.grantTableService = grantTableService;
		this.deptClosureHealthInspector = deptClosureHealthInspector;
	}

	@OperationLog(targetType = "DEPT_CLOSURE_HEALTH", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.DEPT_CLOSURE, operation = OperationLogKind.QUERY)
	@Operation(summary = "检查部门闭包表健康状态")
	@PreAuthorize("@auth.decide('sys:dept:diagnose')")
	@GetMapping("/closure/health")
	public Result<DeptClosureHealthVO> closureHealth() {
		DeptClosureHealthVO healthVO = deptClosureHealthInspector.inspect();
		return Result.success(healthVO);
	}

	@Operation(summary = "查询部门已分配角色")
	@PreAuthorize("@auth.decide('sys:dept:query')")
	@GetMapping("/{deptId}/roles")
	public Result<List<RoleReferenceVO>> listRoles(@PathVariable("deptId") Long deptId) {
		List<RoleReferenceVO> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.DEPT, deptId);
		return Result.success(rows);
	}

	@OperationLog(targetType = "DEPT_ROLE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.GRANT_TABLE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖分配部门角色", description = "全量覆盖；变更后触发授权失效")
	@PreAuthorize("@auth.decide('sys:dept:update')")
	@PutMapping("/{deptId}/roles")
	public Result<Void> replaceRoles(@PathVariable("deptId") Long deptId,
			@Valid @RequestBody GrantTableAssignRoleForm form) {
		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.DEPT, deptId, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
