package com.auth.service.system.schedule.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.query.LogJobQuery;
import com.auth.service.system.schedule.model.vo.LogJobDetailVO;
import com.auth.service.system.schedule.model.vo.LogJobPageVO;
import com.auth.service.system.schedule.service.LogJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 定时任务-日志
 *
 * @author Bunny
 */
@Tag(name = "定时任务-日志", description = "任务执行日志查询与清理")
@RequestMapping("/api/system/log/job")
@RestController
public class LogJobController {

	private final LogJobService logJobService;

	public LogJobController(LogJobService logJobService) {
		this.logJobService = logJobService;
	}

	@Operation(summary = "分页查询任务日志")
	@PreAuthorize("@auth.decide('schedule:joblog:query')")
	@GetMapping("page")
	public Result<PageResponse<LogJobPageVO>> page(LogJobQuery query) {
		PageResponse<LogJobPageVO> response = logJobService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询任务日志详情")
	@PreAuthorize("@auth.decide('schedule:joblog:detail')")
	@GetMapping("{id}")
	public Result<LogJobDetailVO> detail(@PathVariable("id") Long id) {
		LogJobDetailVO detailVO = logJobService.getDetail(id);

		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除任务日志")
	@PreAuthorize("@auth.decide('schedule:joblog:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logJobService.removeByIds(ids);

		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
