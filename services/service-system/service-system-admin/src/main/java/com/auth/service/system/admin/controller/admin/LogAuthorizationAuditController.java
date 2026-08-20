package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.log.LogAuthorizationAuditQuery;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
import com.auth.service.system.admin.service.admin.LogAuthorizationAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 权限决策审计日志
 *
 * @author Bunny
 */
@Tag(name = "权限决策审计日志", description = "授权决策审计查询与清理")
@RequiredArgsConstructor
@RequestMapping("/api/system/log/authorization-audit")
@RestController
public class LogAuthorizationAuditController {

	private final LogAuthorizationAuditService logAuthorizationAuditService;

	@Operation(summary = "分页查询权限决策审计")
	@PreAuthorize("@auth.decide('log:authaudit:query')")
	@GetMapping("page")
	public Result<PageResponse<LogAuthorizationAuditPageVO>> page(LogAuthorizationAuditQuery query) {
		PageResponse<LogAuthorizationAuditPageVO> response = logAuthorizationAuditService.getPage(query);
		return Result.success(response);
	}

	@Operation(summary = "查询权限决策审计详情")
	@PreAuthorize("@auth.decide('log:authaudit:detail')")
	@GetMapping("{id}")
	public Result<LogAuthorizationAuditDetailVO> detail(@PathVariable("id") Long id) {
		LogAuthorizationAuditDetailVO detail = logAuthorizationAuditService.getDetail(id);
		return Result.success(detail);
	}

	@Operation(summary = "批量删除权限决策审计")
	@PreAuthorize("@auth.decide('log:authaudit:delete')")
	@DeleteMapping
	public Result<String> batchDelete(@RequestBody List<Long> ids) {
		logAuthorizationAuditService.removeBatchByIds(ids);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
