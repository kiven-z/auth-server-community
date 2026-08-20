package com.auth.service.system.authorization.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 授权失效幂等事件
 *
 * @author Bunny
 */
@Tag(name = "授权失效幂等事件", description = "幂等事件分页、详情与释放占位")
@RequiredArgsConstructor
@RequestMapping("/api/system/ops/authorization-invalidation/event")
@RestController
public class AuthorizationInvalidationEventOpsController {

	private final AuthorizationInvalidationEventQueryService authorizationInvalidationEventQueryService;

	private final AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	@Operation(summary = "分页查询授权失效幂等事件")
	@PreAuthorize("@auth.decide('ops:invalidationevent:query')")
	@GetMapping("page")
	public Result<PageResponse<AuthorizationInvalidationEventPageVO>> page(AuthorizationInvalidationEventQuery query) {
		PageResponse<AuthorizationInvalidationEventPageVO> page = authorizationInvalidationEventQueryService
			.getPage(query);
		return Result.success(page);
	}

	@Operation(summary = "查询授权失效幂等事件详情")
	@PreAuthorize("@auth.decide('ops:invalidationevent:detail')")
	@GetMapping("{id}")
	public Result<AuthorizationInvalidationEventDetailVO> detail(@PathVariable("id") Long id) {
		AuthorizationInvalidationEventDetailVO detail = authorizationInvalidationEventQueryService.getDetail(id);
		return Result.success(detail);
	}

	@OperationLog(targetType = "AUTH_INVALIDATION_EVENT", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.AUTH_INVALIDATION_EVENT, operation = OperationLogKind.UPDATE)
	@Operation(summary = "释放授权失效幂等事件占位", description = "释放 processing 状态占位")
	@PreAuthorize("@auth.decide('ops:invalidationevent:releaseclaim')")
	@PostMapping("{id}/release-claim")
	public Result<Boolean> releaseClaim(@PathVariable("id") Long id) {
		OperationLogContext.setTargetId(id);

		// 释放 processing 占位，允许 Outbox 重新投递
		boolean released = authorizationInvalidationEventOpsService.releaseProcessingClaim(id);
		return Result.success(released, OPERATION_SUCCESS.getMessage());
	}

}
