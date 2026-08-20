package com.auth.service.system.file.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.system.file.service.FileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件记录内部
 *
 * @author Bunny
 */
@InternalApi
@Tag(name = "文件记录内部", description = "跨模块文件归属校验与撤销")
@RequiredArgsConstructor
@RequestMapping("/api/system/file/inner/file-records")
@RestController
public class InternalFileRecordController {

	private final FileRecordService fileRecordService;

	@Operation(summary = "校验文件地址归属")
	@PostMapping("/ownership/verify")
	public Result<Void> verifyOwnership(@Valid @RequestBody OwnedFileAssertByUrlRequest request) {
		fileRecordService.assertOwnedFileUrl(request);
		return Result.success();
	}

	@Operation(summary = "撤销用户活跃文件")
	@PostMapping("/revoke")
	public Result<Void> revokeOwnedFile(@Valid @RequestBody OwnedFileDeleteByUrlRequest request) {
		fileRecordService.tryDeleteOwnedByUrl(request);
		return Result.success();
	}

}
