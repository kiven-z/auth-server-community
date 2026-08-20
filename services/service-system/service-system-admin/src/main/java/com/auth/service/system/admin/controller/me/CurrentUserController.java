package com.auth.service.system.admin.controller.me;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.ip.IpAddressService;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.service.system.admin.model.form.me.MeAvatarUpdateForm;
import com.auth.service.system.admin.model.form.me.MeProfileUpdateForm;
import com.auth.service.system.admin.model.form.user.SysUserChangePasswordForm;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeOrgBindingsVO;
import com.auth.service.system.admin.model.vo.me.MeProfileVO;
import com.auth.service.system.admin.model.vo.me.MeUserSessionVO;
import com.auth.service.system.admin.service.me.MeProfileService;
import com.auth.service.system.admin.service.me.MeSecurityService;
import com.auth.service.system.admin.support.user.UserPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 当前用户
 *
 * @author Bunny
 */
@AuthenticatedApi
@Tag(name = "当前用户", description = "登录用户自助操作")
@RequiredArgsConstructor
@RequestMapping("/api/system/me")
@RestController
public class CurrentUserController {

	private final IpAddressService ipAddressService;

	private final UserPasswordService userPasswordService;

	private final MeSecurityService meSecurityService;

	private final MeProfileService meProfileService;

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.QUERY)
	@Operation(summary = "查询当前用户展示资料")
	@GetMapping("/profile")
	public Result<MeProfileVO> getMyProfile() {
		MeProfileVO data = meProfileService.getMyProfile();
		return Result.success(data);
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.QUERY)
	@Operation(summary = "查询当前用户组织任职")
	@GetMapping("/org-bindings")
	public Result<MeOrgBindingsVO> getMyOrgBindings() {
		MeOrgBindingsVO data = meProfileService.getMyOrgBindings();
		return Result.success(data);
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新当前用户个人资料")
	@PutMapping("/profile")
	public Result<String> updateMyProfile(@Valid @RequestBody MeProfileUpdateForm form) {
		meProfileService.updateMyProfile(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新当前用户头像")
	@PutMapping("/avatar")
	public Result<String> updateMyAvatar(@Valid @RequestBody MeAvatarUpdateForm form) {
		meProfileService.updateMyAvatar(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新当前用户密码")
	@PutMapping("/password")
	public Result<String> changeOwnPassword(@Valid @RequestBody SysUserChangePasswordForm form,
			HttpServletRequest request) {
		String changeIp = ipAddressService.resolveIpInfo(request).getIpAddr();
		userPasswordService.changeOwnPassword(form, changeIp);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.QUERY)
	@Operation(summary = "查询当前用户活跃会话")
	@GetMapping("/sessions")
	public Result<List<MeUserSessionVO>> listMySessions() {
		List<MeUserSessionVO> data = meSecurityService.listMySessions();
		return Result.success(data);
	}

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.DELETE)
	@Operation(summary = "踢出当前用户指定会话")
	@PostMapping("/sessions/{sessionId}/kick")
	public Result<Void> kickMySession(@PathVariable("sessionId") String sessionId) {
		meSecurityService.kickSession(sessionId);
		return Result.success();
	}

	@OperationLog(targetType = "LOGIN_LOG", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.QUERY)
	@Operation(summary = "分页查询当前用户登录日志")
	@GetMapping("/login-logs/page")
	public Result<PageResponse<MeLoginLogPageVO>> loginLogPage(MeLoginLogPageQuery query) {
		PageResponse<MeLoginLogPageVO> response = meSecurityService.getLoginLogPage(query);
		return Result.success(response);
	}

}
