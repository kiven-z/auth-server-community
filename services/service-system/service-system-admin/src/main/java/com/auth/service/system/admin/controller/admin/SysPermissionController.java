package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.api.port.FileExportTaskOperations;
import com.auth.module.file.export.definition.ExportColumns;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.audit.operationlog.AuditServiceDomain;
import com.auth.module.security.contract.audit.operationlog.OperationLog;
import com.auth.module.security.contract.audit.operationlog.OperationLogKind;
import com.auth.module.security.contract.bizcode.PlatformBizCodes;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetRow;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetService;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageVO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import com.auth.service.system.admin.support.export.AdminFileExportTaskCreateRequests;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

	private final FileExportTaskOperations fileExportTaskOperations;

	@Operation(summary = "导出权限前置检查", description = "按筛选条件估算行数并返回导出方式")
	@PreAuthorize("@auth.decide('sys:permission:export')")
	@PostMapping("/export/pre-check")
	public Result<ExportPreCheckResult> exportPreCheck(@Validated @RequestBody SysPermissionQuery filter) {
		ExportPreCheckRequest request = AdminFileExportTaskCreateRequests.preCheck(PlatformBizCodes.SYS_PERMISSION,
				filter);
		ExportPreCheckResult result = fileExportTaskOperations.preCheck(request);
		return Result.success(result);
	}

	@Operation(summary = "导出权限 Excel", description = "按筛选条件提交异步导出任务")
	@PreAuthorize("@auth.decide('sys:permission:export')")
	@PostMapping("/export/async")
	public Result<FileExportTaskCreateResult> exportAsync(@Validated @RequestBody SysPermissionQuery filter,
			@Parameter(description = "幂等请求ID") @RequestParam(required = false) String requestId) {
		FileExportTaskCreateRequest xlsx = AdminFileExportTaskCreateRequests.xlsx(PlatformBizCodes.SYS_PERMISSION,
				filter, requestId, SecurityUserUtils.getUserId(),
				ExportColumns.fromExcelProperties(SysPermissionSpreadsheetRow.class));

		FileExportTaskCreateResult result = fileExportTaskOperations.createTask(xlsx);
		return Result.success(result);
	}

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
