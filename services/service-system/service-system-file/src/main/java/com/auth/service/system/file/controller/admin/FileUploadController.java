package com.auth.service.system.file.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.form.MultipleFileUploadForm;
import com.auth.service.system.file.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.file.model.constants.FileAuditBizModule.SYS_FILE;

/**
 * 文件上传
 *
 * @author Bunny
 */
@Tag(name = "文件上传", description = "管理端文件上传")
@RequiredArgsConstructor
@RequestMapping("/api/system/file")
@RestController
public class FileUploadController {

	private final FileUploadService fileUploadService;

	@AuthenticatedApi
	@OperationLog(targetType = "FILE_UPLOAD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.CREATE)
	@Operation(summary = "上传单个文件")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<FileUploadResultDTO> upload(@Valid FileUploadForm form) {
		FileUploadResultDTO response = fileUploadService.upload(form);
		return Result.success(response);
	}

	@AuthenticatedApi
	@OperationLog(targetType = "FILE_UPLOAD_MULTIPLE", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.CREATE)
	@Operation(summary = "上传多个文件")
	@PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<List<FileUploadResultDTO>> uploadMultiple(@Valid MultipleFileUploadForm form) {
		List<FileUploadResultDTO> responses = fileUploadService.uploadMultiple(form);
		return Result.success(responses);
	}

}
