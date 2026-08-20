package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetService;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageVO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 系统权限管理
 *
 * @author Bunny
 */
@Tag(name = "系统权限管理", description = "权限码 CRUD 与启停")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/permission")
@RestController
public class SysPermissionController {

	private final SysPermissionService sysPermissionService;

	private final SysPermissionSpreadsheetService sysPermissionSpreadsheetService;

	@Operation(summary = "下载权限导入模板")
	@PreAuthorize("@auth.decide('sys:permission:import')")
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> importTemplate() throws IOException {
		return sysPermissionSpreadsheetService.downloadImportTemplate();
	}

	@Operation(summary = "导入权限 Excel")
	@PreAuthorize("@auth.decide('sys:permission:import')")
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<SpreadsheetImportResult> importExcel(@RequestPart("file") MultipartFile file) throws IOException {
		return Result.success(sysPermissionSpreadsheetService.importExcel(file));
	}

	@Operation(summary = "分页查询权限")
	@PreAuthorize("@auth.decide('sys:permission:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysPermissionPageVO>> page(SysPermissionQuery query) {
		PageResponse<SysPermissionPageVO> response = sysPermissionService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询权限详情")
	@PreAuthorize("@auth.decide('sys:permission:detail')")
	@GetMapping("/{id}")
	public Result<SysPermissionDetailVO> detail(@PathVariable("id") Long id) {
		SysPermissionDetailVO vo = sysPermissionService.getDetail(id);
		return Result.success(vo);
	}

	@Operation(summary = "新增权限")
	@PreAuthorize("@auth.decide('sys:permission:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysPermissionForm form) {
		sysPermissionService.createBatchFromImport(List.of(form));
		return Result.success();
	}

	@OperationLog(targetType = "PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_PERMISSION, operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新权限")
	@PreAuthorize("@auth.decide('sys:permission:update')")
	@PutMapping
	public Result<Void> update(@Validated(UpdateGroup.class) @RequestBody SysPermissionForm form) {
		sysPermissionService.update(form);
		return Result.success();
	}

	@OperationLog(targetType = "PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_PERMISSION, operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停权限")
	@PreAuthorize("@auth.decide('sys:permission:update')")
	@PutMapping("/status")
	public Result<Void> batchUpdateStatus(@Valid @RequestBody IdsEnableStatusForm form) {
		sysPermissionService.batchUpdateStatus(form);
		return Result.success();
	}

	@OperationLog(targetType = "PERMISSION", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = PlatformBizCodes.SYS_PERMISSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "删除权限")
	@PreAuthorize("@auth.decide('sys:permission:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysPermissionService.deleteById(id);
		return Result.success();
	}

}
