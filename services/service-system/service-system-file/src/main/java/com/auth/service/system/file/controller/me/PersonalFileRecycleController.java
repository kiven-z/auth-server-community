package com.auth.service.system.file.controller.me;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.file.model.constants.FileAuditBizModule.SYS_FILE;

/**
 * 个人文件回收站
 *
 * @author Bunny
 */
@AuthenticatedApi
@Tag(name = "个人文件回收站", description = "当前用户回收站查询、恢复与彻底删除")
@RequestMapping("/api/system/me/file/recycle")
@RestController
public class PersonalFileRecycleController {

	private final FileRecordQueryService fileRecordQueryService;

	private final FileRecycleService fileRecycleService;

	public PersonalFileRecycleController(FileRecordQueryService fileRecordQueryService,
			FileRecycleService fileRecycleService) {
		this.fileRecordQueryService = fileRecordQueryService;
		this.fileRecycleService = fileRecycleService;
	}

	@Operation(summary = "分页查询回收站文件")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		// 回收站只能看到自己的和自己删除的
		Long ownerUserId = SecurityUserUtils.getUserId();
		query.setOwnerUserId(ownerUserId);
		query.setDeleteSources(FileDeleteSource.userRecycleSourceCodes());
		// 能被删除的都是私有文件
		query.setIsPrivate(true);

		PageResponse<FileRecordPageVO> pageResult = fileRecordQueryService.getPage(query, true);
		return Result.success(pageResult);
	}

	@Operation(summary = "查询回收站文件详情")
	@GetMapping("/{id}")
	public Result<FileRecordDetailVO> detail(@PathVariable("id") Long id) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		// 能被删除的都是私有文件
		List<String> deleteSources = FileDeleteSource.userRecycleSourceCodes();

		FileRecordDetailVO detail = fileRecordQueryService.getDetail(id, ownerUserId, true, true, deleteSources);
		return Result.success(detail);
	}

	@OperationLog(targetType = "FILE_RECYCLE_RESTORE", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "批量恢复回收站文件")
	@PostMapping("/restore")
	public Result<Void> restore(@RequestBody List<Long> ids) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		List<String> deleteSources = FileDeleteSource.userRecycleSourceCodes();

		fileRecycleService.restoreByIds(ids, ownerUserId, deleteSources);
		return Result.success();
	}

	@OperationLog(targetType = "FILE_RECYCLE_PURGE", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "批量彻底删除回收站文件")
	@DeleteMapping("/purge")
	public Result<Void> purge(@RequestBody List<Long> ids) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		List<String> deleteSources = FileDeleteSource.userRecycleSourceCodes();

		fileRecycleService.purgeByIds(ids, ownerUserId, deleteSources);
		return Result.success();
	}

}
