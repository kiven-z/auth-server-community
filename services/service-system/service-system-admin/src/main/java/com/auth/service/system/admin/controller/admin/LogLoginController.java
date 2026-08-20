package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogDetailVO;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.service.admin.LogLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 登录日志
 *
 * @author Bunny
 */
@Tag(name = "登录日志", description = "登录成功失败记录查询与清理")
@RequiredArgsConstructor
@RequestMapping("/api/system/log/login")
@RestController
public class LogLoginController {

	private final LogLoginService logLoginService;

	@Operation(summary = "分页查询登录日志")
	@PreAuthorize("@auth.decide('log:login:query')")
	@GetMapping("page")
	public Result<PageResponse<LogLoginLogPageVO>> page(LogLoginLogQuery query) {
		PageResponse<LogLoginLogPageVO> response = logLoginService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询登录日志详情")
	@PreAuthorize("@auth.decide('log:login:detail')")
	@GetMapping("{id}")
	public Result<LogLoginLogDetailVO> detail(@PathVariable("id") Long id) {
		LogLoginLogDetailVO detailVO = logLoginService.getDetail(id);
		return Result.success(detailVO);
	}

	@Operation(summary = "批量删除登录日志")
	@PreAuthorize("@auth.decide('log:login:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logLoginService.removeBatchByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
