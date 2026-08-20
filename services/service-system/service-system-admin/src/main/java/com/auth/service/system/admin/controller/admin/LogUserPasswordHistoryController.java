package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.log.LogUserPasswordHistoryQuery;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryPageVO;
import com.auth.service.system.admin.service.admin.LogUserPasswordHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 密码历史日志
 *
 * @author Bunny
 */
@Tag(name = "密码历史日志", description = "用户密码变更历史查询")
@RequiredArgsConstructor
@RequestMapping("/api/system/log/password-history")
@RestController
public class LogUserPasswordHistoryController {

	private final LogUserPasswordHistoryService logUserPasswordHistoryService;

	@Operation(summary = "分页查询密码历史日志")
	@PreAuthorize("@auth.decide('log:passwordhistory:query')")
	@GetMapping("page")
	public Result<PageResponse<LogUserPasswordHistoryPageVO>> page(LogUserPasswordHistoryQuery query) {
		PageResponse<LogUserPasswordHistoryPageVO> response = logUserPasswordHistoryService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询密码历史日志详情")
	@PreAuthorize("@auth.decide('log:passwordhistory:detail')")
	@GetMapping("{id}")
	public Result<LogUserPasswordHistoryDetailVO> detail(@PathVariable("id") Long id) {
		LogUserPasswordHistoryDetailVO detailVO = logUserPasswordHistoryService.getDetail(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除密码历史日志")
	@PreAuthorize("@auth.decide('log:passwordhistory:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logUserPasswordHistoryService.removeBatchByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
