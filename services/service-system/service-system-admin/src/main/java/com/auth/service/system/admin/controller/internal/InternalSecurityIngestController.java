package com.auth.service.system.admin.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.contract.dto.AuthorizationAuditIngestRequest;
import com.auth.module.security.contract.dto.OperationLogIngestRequest;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.service.system.admin.ingest.SecurityIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部-安全审计上报
 *
 * @author Bunny
 */
@Tag(name = "内部-安全审计上报", description = "接收授权决策审计与操作日志并落库")
@RequestMapping("/api/system/inner")
@RestController
public class InternalSecurityIngestController {

	private final SecurityIngestService securityIngestService;

	public InternalSecurityIngestController(SecurityIngestService securityIngestService) {
		this.securityIngestService = securityIngestService;
	}

	@InternalApi
	@Operation(summary = "追加授权审计记录")
	@PostMapping("/authorization-audit/records")
	public Result<Void> appendAuthorizationAudit(@RequestBody AuthorizationAuditIngestRequest body) {
		SecurityAuthorizationAuditPayloadEvent event = body.toPayloadEvent();
		securityIngestService.append(event);
		return Result.success();
	}

	@InternalApi
	@Operation(summary = "追加操作日志")
	@PostMapping("/operation-log/records")
	public Result<Void> appendOperationLog(@RequestBody OperationLogIngestRequest body) {
		securityIngestService.append(body.toPayload());
		return Result.success();
	}

}
