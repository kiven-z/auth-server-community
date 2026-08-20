package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.user.SysUserScopeVO;
import com.auth.service.system.admin.service.admin.SysUserScopeService;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 用户授权
 *
 * @author Bunny
 */
@Tag(name = "用户授权", description = "用户数据范围覆盖与清除")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/user")
@RestController
public class SysUserAuthorizationController {

	private final SysUserScopeService sysUserScopeService;

	@Operation(summary = "查询用户数据范围", description = "未配置时 data=null，表示继承角色")
	@PreAuthorize("@auth.decide('sys:user:query')")
	@GetMapping("/{userId}/scope")
	public Result<SysUserScopeVO> getScope(@PathVariable("userId") Long userId) {
		SysUserScopeVO userScopeVO = sysUserScopeService.getByUserId(userId);
		return Result.success(userScopeVO);
	}

	@OperationLog(targetType = "USER_SCOPE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_USER_SCOPE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "保存用户数据范围", description = "有则改无则插；覆盖角色范围并触发授权失效")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping("/{userId}/scope")
	public Result<Void> upsertScope(@PathVariable("userId") Long userId, @Valid @RequestBody SysDataScopeForm form) {
		sysUserScopeService.upsert(userId, form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_SCOPE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_USER_SCOPE, operation = OperationLogKind.DELETE)
	@Operation(summary = "清除用户数据范围覆盖", description = "删除配置行并恢复角色继承；无行时幂等")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@DeleteMapping("/{userId}/scope")
	public Result<Void> clearScope(@PathVariable("userId") Long userId) {
		sysUserScopeService.clearByUserId(userId);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
