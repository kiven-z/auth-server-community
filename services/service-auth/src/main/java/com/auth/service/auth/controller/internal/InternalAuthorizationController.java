package com.auth.service.auth.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;
import com.auth.service.auth.service.AdminAuthorizationService;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import com.auth.service.auth.service.AuthorizationInvalidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 授权内部
 *
 * @author Bunny
 */
@Tag(name = "授权内部", description = "生效码查询、授权失效执行与事件")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth/inner/authorization")
@RestController
public class InternalAuthorizationController {

	private final AdminAuthorizationService adminAuthorizationService;

	private final AuthorizationInvalidationService authorizationInvalidationService;

	private final AuthorizationInvalidationEventService authorizationInvalidationEventService;

	@InternalApi
	@Operation(summary = "查询用户生效角色码与权限码", description = "供内部服务组装动态菜单等")
	@GetMapping("/principal/{userId}/effective-codes")
	public Result<EffectiveCodesVO> effectiveCodes(@PathVariable("userId") Long userId) {
		return Result.success(adminAuthorizationService.getEffectiveCodes(userId));
	}

	@InternalApi
	@Operation(summary = "执行授权失效", description = "反查受影响用户，递增权限版本并刷新或驱逐授权画像")
	@PostMapping("/invalidate")
	public Result<AuthorizationInvalidateResponse> invalidate(
			@Valid @RequestBody AuthorizationInvalidateRequest request) {
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);
		return Result.success(response);
	}

	@InternalApi
	@Operation(summary = "分页查询授权失效幂等事件")
	@GetMapping("/invalidation/event/page")
	public Result<PageResponse<AuthorizationInvalidationEventPageVO>> invalidationEventPage(
			AuthorizationInvalidationEventQuery query) {
		PageResponse<AuthorizationInvalidationEventPageVO> page = authorizationInvalidationEventService.getPage(query);
		return Result.success(page);
	}

	@InternalApi
	@Operation(summary = "查询授权失效幂等事件详情")
	@GetMapping("/invalidation/event/{id}")
	public Result<AuthorizationInvalidationEventDetailVO> invalidationEventDetail(@PathVariable("id") Long id) {
		AuthorizationInvalidationEventDetailVO detailVO = authorizationInvalidationEventService.getDetail(id);
		return Result.success(detailVO);
	}

	@InternalApi
	@Operation(summary = "查询授权失效幂等事件统计")
	@GetMapping("/invalidation/event/summary")
	public Result<AuthorizationInvalidationEventStatsVO> invalidationEventSummary() {
		AuthorizationInvalidationEventStatsVO statsVO = authorizationInvalidationEventService.getStats();
		return Result.success(statsVO);
	}

	@InternalApi
	@Operation(summary = "释放授权失效幂等事件占位", description = "释放 processing 状态占位")
	@PostMapping("/invalidation/event/{eventId}/release-claim")
	public Result<Boolean> releaseInvalidationEventClaim(@PathVariable("eventId") String eventId) {
		boolean released = authorizationInvalidationEventService.releaseProcessingClaim(eventId);

		return Result.success(released);
	}

}
