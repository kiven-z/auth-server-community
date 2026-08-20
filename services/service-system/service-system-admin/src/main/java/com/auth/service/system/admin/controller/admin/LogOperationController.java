package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.log.LogOperationQuery;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import com.auth.service.system.admin.service.admin.LogOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 操作日志
 *
 * @author Bunny
 */
@Tag(name = "操作日志", description = "操作审计查询与清理")
@RequiredArgsConstructor
@RequestMapping("/api/system/log/operation")
@RestController
public class LogOperationController {

	private final LogOperationService logOperationService;

	@Operation(summary = "分页查询操作日志")
	@PreAuthorize("@auth.decide('log:operation:query')")
	@GetMapping("page")
	public Result<PageResponse<LogOperationPageVO>> page(LogOperationQuery query) {
		PageResponse<LogOperationPageVO> response = logOperationService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询操作日志详情")
	@PreAuthorize("@auth.decide('log:operation:detail')")
	@GetMapping("{id}")
	public Result<LogOperationDetailVO> detail(@PathVariable("id") Long id) {
		LogOperationDetailVO detail = logOperationService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "批量删除操作日志")
	@PreAuthorize("@auth.decide('log:operation:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logOperationService.removeBatchByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
