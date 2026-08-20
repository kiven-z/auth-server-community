package com.auth.service.system.authorization.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationFailureRateTrendQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationFailureRateTrendVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationStatsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 授权失效运维统计
 *
 * @author Bunny
 */
@Tag(name = "授权失效运维统计", description = "Outbox 与幂等事件统计摘要")
@RequiredArgsConstructor
@RequestMapping("/api/system/ops/authorization-invalidation")
@RestController
public class AuthorizationInvalidationStatsOpsController {

	private final AuthorizationInvalidationStatsQueryService statsQueryService;

	@Operation(summary = "查询授权失效运维统计")
	@PreAuthorize("@auth.decide('ops:invalidationoutbox:query')")
	@GetMapping("summary")
	public Result<AuthorizationInvalidationSummaryVO> summary() {
		AuthorizationInvalidationSummaryVO summary = statsQueryService.getSummary();
		return Result.success(summary);
	}

	@Operation(summary = "查询授权失效失败率趋势")
	@PreAuthorize("@auth.decide('ops:invalidationoutbox:query')")
	@GetMapping("stats/failure-rate/trend")
	public Result<AuthorizationInvalidationFailureRateTrendVO> failureRateTrend(
			@Validated AuthorizationInvalidationFailureRateTrendQuery query) {
		AuthorizationInvalidationFailureRateTrendVO trend = statsQueryService.getFailureRateTrend(query);
		return Result.success(trend);
	}

}
