package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;
import com.auth.service.system.admin.support.dept.DeptClosureHealthInspector;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门扩展管理
 *
 * @author Bunny
 */
@Tag(name = "部门扩展管理", description = "闭包健康检查")
@RequestMapping("/api/system/dept")
@RestController
public class SysDeptManageController {

	private final DeptClosureHealthInspector deptClosureHealthInspector;

	public SysDeptManageController(DeptClosureHealthInspector deptClosureHealthInspector) {
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

}
