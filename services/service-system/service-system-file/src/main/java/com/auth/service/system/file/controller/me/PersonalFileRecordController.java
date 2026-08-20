package com.auth.service.system.file.controller.me;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.delivery.FileDelivery;
import com.auth.module.file.delivery.FileDownloadNames;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecordQueryService;
import com.auth.service.system.file.service.FileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.file.model.constants.FileAuditBizModule.SYS_FILE;

/**
 * 个人文件
 *
 * @author Bunny
 */
@AuthenticatedApi
@Tag(name = "个人文件", description = "当前用户文件查询、下载与删除")
@RequestMapping("/api/system/me/file")
@RestController
public class PersonalFileRecordController {

	private final FileRecordQueryService fileRecordQueryService;

	private final FileRecordService fileRecordService;

	public PersonalFileRecordController(FileRecordQueryService fileRecordQueryService,
			FileRecordService fileRecordService) {
		this.fileRecordQueryService = fileRecordQueryService;
		this.fileRecordService = fileRecordService;
	}

	@Operation(summary = "分页查询文件")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		// 固定查询人
		Long ownerUserId = SecurityUserUtils.getUserId();
		query.setOwnerUserId(ownerUserId);
		query.setDeleteSource(null);

		PageResponse<FileRecordPageVO> pageResult = fileRecordQueryService.getPage(query, false);
		return Result.success(pageResult);
	}

	@Operation(summary = "查询文件详情")
	@GetMapping("/{id}")
	public Result<FileRecordDetailVO> detail(@PathVariable("id") Long id) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		FileRecordDetailVO detail = fileRecordQueryService.getDetail(id, ownerUserId, null, false, null);
		return Result.success(detail);
	}

	@OperationLog(targetType = "FILE_RECORD_DOWNLOAD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.EXPORT)
	@Operation(summary = "批量下载文件")
	@PostMapping("/download")
	public ResponseEntity<StreamingResponseBody> batchDownload(@RequestBody List<Long> ids) {
		Long ownerUserId = SecurityUserUtils.getUserId();

		StreamingResponseBody body = fileRecordService.batchDownload(ids, ownerUserId);
		String filename = FileDownloadNames.batchZip("file-records");
		return FileDelivery.deliver(body, filename, FileDelivery.APPLICATION_ZIP);
	}

	@OperationLog(targetType = "FILE_RECORD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "批量删除文件")
	@DeleteMapping()
	public Result<Void> delete(@RequestBody List<Long> ids) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		fileRecordService.deleteByIds(ids, ownerUserId, FileDeleteSource.USER_SELF.getCode());
		return Result.success();
	}

}
