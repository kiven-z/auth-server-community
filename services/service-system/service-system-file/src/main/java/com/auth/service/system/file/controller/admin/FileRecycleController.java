package com.auth.service.system.file.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecordQueryService;
import com.auth.service.system.file.service.FileRecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.file.model.constants.FileAuditBizModule.SYS_FILE;

/**
 * 文件回收站
 *
 * @author Bunny
 */
@Tag(name = "文件回收站", description = "管理端回收站查询、恢复与彻底删除")
@RequestMapping("/api/system/file/recycle")
@RestController
public class FileRecycleController {

	private final FileRecordQueryService fileRecordQueryService;

	private final FileRecycleService fileRecycleService;

	public FileRecycleController(FileRecordQueryService fileRecordQueryService, FileRecycleService fileRecycleService) {
		this.fileRecordQueryService = fileRecordQueryService;
		this.fileRecycleService = fileRecycleService;
	}

	@Operation(summary = "分页查询回收站文件")
	@PreAuthorize("@auth.decide('sys:file:recycle:query')")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		// 能被删除的都是私有文件
		query.setIsPrivate(true);

		PageResponse<FileRecordPageVO> pageResult = fileRecordQueryService.getPage(query, true);
		return Result.success(pageResult);
	}

	@Operation(summary = "查询回收站文件详情")
	@PreAuthorize("@auth.decide('sys:file:query')")
	@GetMapping("/{id}")
	public Result<FileRecordDetailVO> detail(@PathVariable("id") Long id) {
		// 能被删除的都是私有文件
		FileRecordDetailVO detail = fileRecordQueryService.getDetail(id, null, true, true, null);
		return Result.success(detail);
	}

	@OperationLog(targetType = "FILE_RECYCLE_RESTORE", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量恢复回收站文件")
	@PreAuthorize("@auth.decide('sys:file:recycle:restore')")
	@PostMapping("/restore")
	public Result<Void> restore(@RequestBody List<Long> ids) {
		fileRecycleService.restoreByIds(ids, null, null);
		return Result.success();
	}

	@OperationLog(targetType = "FILE_RECYCLE_PURGE", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "批量彻底删除回收站文件")
	@PreAuthorize("@auth.decide('sys:file:recycle:purge')")
	@DeleteMapping("/purge")
	public Result<Void> purge(@RequestBody List<Long> ids) {
		fileRecycleService.purgeByIds(ids, null, null);
		return Result.success();
	}

}
