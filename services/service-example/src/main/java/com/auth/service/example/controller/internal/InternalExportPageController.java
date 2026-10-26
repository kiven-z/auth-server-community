package com.auth.service.example.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.ExportPageDataDTO;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.profile.AuthProfile;
import com.auth.service.example.export.ExampleOrderExportPageFetcher;
import com.auth.service.example.model.request.InternalExportPageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * 内部导出分页取数（供 file Worker 回调）
 *
 * @author Bunny
 */
@InternalApi
@Tag(name = "内部-示例导出分页取数", description = "供文件导出 Worker 回调")
@RequiredArgsConstructor
@RequestMapping("/api/example/inner/export")
@RestController
public class InternalExportPageController {

	private final ExampleOrderExportPageFetcher exportPageFetcher;

	@Operation(summary = "分页查询导出数据", description = "恢复权限快照后按业务类型路由")
	@PostMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
	public Result<ExportPageDataDTO> page(@Valid @RequestBody InternalExportPageRequest request) {
		AuthProfile profile = FileExportScopeSnapshots.requireAuthProfile(request.getScopeSnapshotJson());
		ExportPageDataDTO data = executeWithScope(profile, () -> exportPageFetcher.fetchPage(request.getBizType(),
				request.getQuerySnapshotJson(), request.getPageNum(), request.getPageSize()));
		return Result.success(data);
	}

	private <T> T executeWithScope(AuthProfile profile, ScopedOperation<T> operation) {
		SecurityContext originalContext = SecurityContextHolder.getContext();
		SecurityContext scopedContext = SecurityContextHolder.createEmptyContext();
		scopedContext
			.setAuthentication(new UsernamePasswordAuthenticationToken(profile, null, Collections.emptyList()));
		SecurityContextHolder.setContext(scopedContext);
		try {
			return operation.execute();
		}
		finally {
			SecurityContextHolder.setContext(originalContext);
		}
	}

	@FunctionalInterface
	private interface ScopedOperation<T> {

		T execute();

	}

}
