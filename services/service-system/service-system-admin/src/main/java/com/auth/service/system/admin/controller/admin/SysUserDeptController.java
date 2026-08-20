package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.service.system.admin.model.form.user.UserDeptAssignForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.user.UserDeptPageVO;
import com.auth.service.system.admin.service.admin.SysUserDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户部门关联
 *
 * @author Bunny
 */
@Tag(name = "用户部门关联", description = "用户与部门绑定查询与维护")
@RequiredArgsConstructor
@RequestMapping("/api/system/user-dept")
@RestController
public class SysUserDeptController {

	private final SysUserDeptService sysUserDeptService;

	@Operation(summary = "分页查询用户部门关联")
	@PreAuthorize("@auth.decide('sys:userdept:query')")
	@GetMapping("/{userId}/page")
	public Result<PageResponse<UserDeptPageVO>> pageDepts(@PathVariable("userId") Long userId,
			UserDeptPageQuery query) {
		PageResponse<UserDeptPageVO> page = sysUserDeptService.getPage(userId, query);
		return Result.success(page);
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户部门关联")
	@PreAuthorize("@auth.decide('sys:userdept:create')")
	@PostMapping("/{userId}")
	public Result<Void> create(@PathVariable("userId") Long userId, @Valid @RequestBody UserDeptAssignForm form) {
		sysUserDeptService.create(userId, form);
		return Result.success();
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新用户部门关联")
	@PreAuthorize("@auth.decide('sys:userdept:update')")
	@PutMapping("/{userId}/{id}")
	public Result<Void> update(@PathVariable("userId") Long userId, @PathVariable("id") Long id,
			@Valid @RequestBody UserDeptAssignForm form) {
		sysUserDeptService.update(userId, id, form);
		return Result.success();
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户部门关联")
	@PreAuthorize("@auth.decide('sys:userdept:delete')")
	@DeleteMapping("/{userId}")
	public Result<Void> removeBatch(@PathVariable("userId") Long userId, @RequestBody List<Long> ids) {
		sysUserDeptService.removeBatch(userId, ids);
		return Result.success();
	}

	@OperationLog(targetType = "USER_DEPT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "清空用户全部部门关联")
	@PreAuthorize("@auth.decide('sys:userdept:delete')")
	@DeleteMapping("/{userId}/all")
	public Result<Void> removeAll(@PathVariable("userId") Long userId) {
		sysUserDeptService.removeAll(userId);
		return Result.success();
	}

}
