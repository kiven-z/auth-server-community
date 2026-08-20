package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.ip.IpAddressService;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.service.system.admin.excel.user.SysUserSpreadsheetService;
import com.auth.service.system.admin.model.form.user.SysUserAdminResetPasswordForm;
import com.auth.service.system.admin.model.form.user.SysUserAvatarUpdateForm;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.service.admin.SysUserQueryService;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.admin.support.user.UserPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 系统用户管理
 *
 * @author Bunny
 */
@Tag(name = "系统用户管理", description = "用户 CRUD、启停、密码与导入")
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class SysUserController {

	private final SysUserService sysUserService;

	private final SysUserQueryService sysUserQueryService;

	private final SysUserSpreadsheetService sysUserSpreadsheetService;

	private final UserPasswordService userPasswordService;

	private final IpAddressService ipAddressService;

	@OperationLog(targetType = "USER_LIST", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_USER, operation = OperationLogKind.QUERY)
	@Operation(summary = "分页查询用户")
	@PreAuthorize("@auth.decide('sys:user:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysUserPageVO>> page(SysUserPageQuery query) {
		PageResponse<SysUserPageVO> response = sysUserQueryService.getPage(query);
		return Result.success(response);
	}

	@AuthenticatedApi
	@Operation(summary = "查询用户档案")
	@GetMapping("/{userId}/profile")
	public Result<SysUserProfileVO> profile(@PathVariable("userId") Long userId) {
		SysUserProfileVO profile = sysUserQueryService.getProfile(userId);
		return Result.success(profile);
	}

	@Operation(summary = "查询用户详情")
	@PreAuthorize("@auth.decide('sys:user:query')")
	@GetMapping("/{userId}/detail")
	public Result<SysUserDetailVO> detail(@PathVariable("userId") Long userId) {
		SysUserDetailVO detail = sysUserQueryService.getDetail(userId);
		return Result.success(detail);
	}

	@AuthenticatedApi
	@Operation(summary = "搜索用户")
	@GetMapping("/search")
	public Result<List<SysUserSearchItemVO>> search(
			@Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
			@Parameter(description = "返回条数上限，默认 20，最大 50") @RequestParam(required = false,
					defaultValue = "20") Integer limit) {

		List<SysUserSearchItemVO> data = sysUserQueryService.searchByKeyword(keyword, limit);
		return Result.success(data);
	}

	@Operation(summary = "下载用户导入模板")
	@PreAuthorize("@auth.decide('sys:user:import')")
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> importTemplate() throws IOException {
		return sysUserSpreadsheetService.downloadImportTemplate();
	}

	@Operation(summary = "导入用户 Excel")
	@PreAuthorize("@auth.decide('sys:user:import')")
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<SpreadsheetImportResult> importExcel(@RequestPart("file") MultipartFile file) throws IOException {
		SpreadsheetImportResult data = sysUserSpreadsheetService.importExcel(file);
		return Result.success(data);
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.CREATE)
	@Operation(summary = "新增用户")
	@PreAuthorize("@auth.decide('sys:user:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysUserForm form) {
		sysUserService.createBatchFromImport(List.of(form));
		return Result.success();
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "重置用户密码")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping("/{id}/password")
	public Result<String> resetPassword(@PathVariable("id") Long id,
			@Valid @RequestBody SysUserAdminResetPasswordForm form, HttpServletRequest request) {
		String changeIp = ipAddressService.resolveIpInfo(request).getIpAddr();
		userPasswordService.resetPasswordByAdmin(id, form, changeIp);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新用户头像")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping("/avatar")
	public Result<String> updateAvatar(@Valid @RequestBody SysUserAvatarUpdateForm form) {
		sysUserService.updateAvatar(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新用户基础资料")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping
	public Result<Void> update(@Validated(UpdateGroup.class) @RequestBody SysUserForm form) {
		sysUserService.update(form);
		return Result.success();
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停用户")
	@PreAuthorize("@auth.decide('sys:user:update')")
	@PutMapping("/status")
	public Result<String> batchUpdateStatus(@Valid @RequestBody SysUserBatchStatusForm form) {
		sysUserService.batchUpdateStatus(form);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

	@OperationLog(targetType = "USER", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_USER,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除用户")
	@PreAuthorize("@auth.decide('sys:user:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		sysUserService.deleteByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
