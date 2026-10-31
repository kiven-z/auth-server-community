package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.audit.operationlog.AuditServiceDomain;
import com.auth.module.security.contract.audit.operationlog.OperationLog;
import com.auth.module.security.contract.audit.operationlog.OperationLogKind;
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

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 用户授权
 *
 * @author Bunny
 */
@Tag(name = "用户授权", description = "用户数据范围覆盖")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/user")
@RestController
public class SysUserAuthorizationController {

	private final SysUserScopeService sysUserScopeService;

	@Operation(summary = "查询用户数据范围")
	@PreAuthorize("@auth.decide('sys:user:query')")
	@GetMapping("/{userId}/scope")
	public Result<List<SysUserScopeVO>> getScope(@PathVariable("userId") Long userId) {
		List<SysUserScopeVO> scopes = sysUserScopeService.getByUserId(userId);
		return Result.success(scopes);
	}

	@OperationLog(targetType = "USER_SCOPE", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.SYS_USER_SCOPE, operation = OperationLogKind.UPDATE)
	@Operation(summary = "覆盖保存用户数据范围", description = "按列表覆盖全部数据维；空数组清空并恢复角色继承；变更后触发一次授权失效")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping("/{userId}/scope")
	public Result<Void> replaceScope(@PathVariable("userId") Long userId,
			@Valid @RequestBody List<@Valid SysDataScopeForm> forms) {
		sysUserScopeService.replace(userId, forms);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
