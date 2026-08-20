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
import com.auth.service.system.admin.excel.post.SysPostSpreadsheetService;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageVO;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import com.auth.service.system.admin.service.admin.SysPostService;
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
 * 系统岗位管理
 *
 * @author Bunny
 */
@Tag(name = "系统岗位管理", description = "岗位 CRUD 与启停")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/system/post")
@RestController
public class SysPostController {

	private final SysPostService sysPostService;

	private final SysPostSpreadsheetService sysPostSpreadsheetService;

	@Operation(summary = "下载岗位导入模板")
	@PreAuthorize("@auth.decide('sys:post:import')")
	@GetMapping("/import/template")
	public ResponseEntity<byte[]> importTemplate() throws IOException {
		return sysPostSpreadsheetService.downloadImportTemplate();
	}

	@Operation(summary = "导入岗位 Excel")
	@PreAuthorize("@auth.decide('sys:post:import')")
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<SpreadsheetImportResult> importExcel(@RequestPart("file") MultipartFile file) throws IOException {
		SpreadsheetImportResult data = sysPostSpreadsheetService.importExcel(file);
		return Result.success(data);
	}

	@Operation(summary = "分页查询岗位")
	@PreAuthorize("@auth.decide('sys:post:query')")
	@GetMapping("/page")
	public Result<PageResponse<SysPostPageVO>> page(SysPostQuery query) {
		PageResponse<SysPostPageVO> response = sysPostService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "搜索岗位")
	@PreAuthorize("@auth.decide('sys:post:query')")
	@GetMapping("/search")
	public Result<List<SysPostSearchItemVO>> search(
			@Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
			@Parameter(description = "启用状态(true=启用，false=停用)") @RequestParam(required = false) Boolean status,
			@Parameter(description = "返回条数上限，默认 20，最大 50") @RequestParam(required = false,
					defaultValue = "20") Integer limit) {
		List<SysPostSearchItemVO> search = sysPostService.search(keyword, status, limit);
		return Result.success(search);
	}

	@Operation(summary = "查询岗位详情")
	@PreAuthorize("@auth.decide('sys:post:query')")
	@GetMapping("/{id}")
	public Result<SysPostDetailVO> detail(@PathVariable("id") Long id) {
		SysPostDetailVO detail = sysPostService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "新增岗位")
	@PreAuthorize("@auth.decide('sys:post:create')")
	@PostMapping
	public Result<Void> create(@Validated(CreateGroup.class) @RequestBody SysPostForm form) {
		sysPostService.createBatchFromImport(List.of(form));
		return Result.success();
	}

	@OperationLog(targetType = "POST", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_POST,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "更新岗位")
	@PreAuthorize("@auth.decide('sys:post:update')")
	@PutMapping
	public Result<Void> update(@Validated(UpdateGroup.class) @RequestBody SysPostForm form) {
		sysPostService.update(form);
		return Result.success();
	}

	@OperationLog(targetType = "POST", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_POST,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量启停岗位")
	@PreAuthorize("@auth.decide('sys:post:update')")
	@PutMapping("/status")
	public Result<Void> batchUpdateStatus(@Valid @RequestBody IdsEnableStatusForm form) {
		sysPostService.batchUpdateStatus(form);
		return Result.success();
	}

	@OperationLog(targetType = "POST", serviceDomain = AuditServiceDomain.SYSTEM, bizModule = PlatformBizCodes.SYS_POST,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "删除岗位")
	@PreAuthorize("@auth.decide('sys:post:delete')")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		sysPostService.deleteById(id);
		return Result.success();
	}

}
