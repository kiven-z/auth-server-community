package com.auth.service.system.authorization.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.system.authorization.model.constants.AuthorizationAuditBizModule;
import com.auth.service.system.authorization.model.form.AuthorizationInvalidationOutboxRetryForm;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxRetryResultVO;
import com.auth.service.system.authorization.ops.AuthorizationInvalidationOutboxManualRetryService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.auth.service.system.authorization.exception.AuthorizationInvalidationOpsResultCode.RETRY_FAILED;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 授权失效 Outbox
 *
 * @author Bunny
 */
@Tag(name = "授权失效 Outbox", description = "Outbox 分页、详情与重试")
@RequiredArgsConstructor
@RequestMapping("/api/system/ops/authorization-invalidation/outbox")
@RestController
public class AuthorizationInvalidationOutboxOpsController {

	private final AuthorizationInvalidationOutboxQueryService authorizationInvalidationOutboxQueryService;

	private final AuthorizationInvalidationOutboxManualRetryService authorizationInvalidationOutboxManualRetryService;

	@Operation(summary = "分页查询授权失效投递")
	@PreAuthorize("@auth.decide('ops:invalidationoutbox:query')")
	@GetMapping("page")
	public Result<PageResponse<AuthorizationInvalidationOutboxPageVO>> page(
			AuthorizationInvalidationOutboxQuery query) {
		PageResponse<AuthorizationInvalidationOutboxPageVO> page = authorizationInvalidationOutboxQueryService
			.getPage(query);
		return Result.success(page);
	}

	@Operation(summary = "查询授权失效投递详情")
	@PreAuthorize("@auth.decide('ops:invalidationoutbox:detail')")
	@GetMapping("{id}")
	public Result<AuthorizationInvalidationOutboxDetailVO> detail(@PathVariable("id") Long id) {
		AuthorizationInvalidationOutboxDetailVO detail = authorizationInvalidationOutboxQueryService.getDetail(id);
		return Result.success(detail);
	}

	@OperationLog(targetType = "AUTH_INVALIDATION_OUTBOX", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = AuthorizationAuditBizModule.AUTH_INVALIDATION_OUTBOX, operation = OperationLogKind.UPDATE)
	@Operation(summary = "重试授权失效投递")
	@PreAuthorize("@auth.decide('ops:invalidationoutbox:retry')")
	@PostMapping("{id}/retry")
	public Result<AuthorizationInvalidationOutboxRetryResultVO> retry(@PathVariable("id") Long id,
			@RequestBody(required = false) AuthorizationInvalidationOutboxRetryForm form) {
		OperationLogContext.setTargetId(id);

		AuthorizationInvalidationOutboxRetryResultVO resultVO = authorizationInvalidationOutboxManualRetryService
			.retryById(id, form);
		if (resultVO.getDispatched() != null && resultVO.getDispatched()) {
			return Result.success(resultVO, OPERATION_SUCCESS.getMessage());
		}
		return Result.success(resultVO, RETRY_FAILED.getMessage());
	}

}
