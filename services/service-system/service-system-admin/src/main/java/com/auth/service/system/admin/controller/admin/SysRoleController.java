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
import com.auth.service.system.admin.excel.role.SysRoleSpreadsheetService;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageVO;
import com.auth.service.system.admin.service.admin.SysRoleService;
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
 * 系统角色管理
 *
 * @author Bunny
 */
@Tag(name = "系统角色管理", description = "角色 CRUD 与启停")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/role")
@RestController
public class SysRoleController {

	private final SysRoleService sysRoleService;

	private final SysRoleSpreadsheetService sysRoleSpreadsheetService;

	@Operation(summary = "下载角色导入模板")
	@PreAuthorize("@auth.decide('sys:role:import')")
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> importTemplate() throws IOException {
		return sysRoleSpreadsheetService.downloadImportTemplate();
	}

	@Operation(summary = "导入角色 Excel")
	@PreAuthorize("@auth.decide('sys:role:import')")
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<SpreadsheetImportResult> importExcel(@RequestPart("file") MultipartFile file) throws IOException {
		SpreadsheetImportResult data = sysRoleSpreadsheetService.importExcel(file);
		return Result.success(data);
	}

	@Operation(summary = "分页查询角色")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysRolePageVO>> page(SysRoleQuery query) {
		PageResponse<SysRolePageVO> response = sysRoleService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询角色详情")
	@PreAuthorize("@auth.decide('sys:role:detail')")
	@GetMapping("/{id}")
	public Result<SysRoleDetailVO> detail(@PathVariable("id") Long id) {
		SysRoleDetailVO vo = sysRoleService.getDetail(id);
		return Result.success(vo);
	}

	@Operation(summary = "新增角色")
	@PreAuthorize("@auth.decide('sys:role:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysRoleForm form) {
		sysRoleService.createBatchFromImport(List.of(form));
		return Result.success();
	}

	@OperationLog(targetType = "ROLE", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_ROLE,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新角色")
	@PreAuthorize("@auth.decide('sys:role:update')")
	@PutMapping
	public Result<Void> update(@Validated(UpdateGroup.class) @RequestBody SysRoleForm form) {
		sysRoleService.update(form);
		return Result.success();
	}

	@OperationLog(targetType = "ROLE", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_ROLE,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停角色")
	@PreAuthorize("@auth.decide('sys:role:update')")
	@PutMapping("/status")
	public Result<Void> batchUpdateStatus(@Valid @RequestBody IdsEnableStatusForm form) {
		sysRoleService.batchUpdateStatus(form);
		return Result.success();
	}

	@OperationLog(targetType = "ROLE", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_ROLE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "删除角色")
	@PreAuthorize("@auth.decide('sys:role:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysRoleService.deleteById(id);
		return Result.success();
	}

	@Operation(summary = "查询角色下拉选项")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/options")
	public Result<List<SysRoleOptionVO>> options(@RequestParam(value = "roleName", required = false) String roleName,
			@RequestParam(value = "roleCode", required = false) String roleCode) {
		List<SysRoleOptionVO> list = sysRoleService.listOptions(roleName, roleCode);
		return Result.success(list);
	}

}
