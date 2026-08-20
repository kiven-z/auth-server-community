package com.auth.service.example.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.example.audit.ExampleAuditBizModule;
import com.auth.service.example.feign.ExampleAuthInternalFeignClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安全集成验证用 HTTP 接口
 *
 * @author Bunny
 */
@Slf4j
@RequestMapping("/api/example")
@RestController
public class ExampleController {

	private final ExampleAuthInternalFeignClient exampleAuthInternalFeignClient;

	private final SecurityProperties securityProperties;

	private final ObjectMapper objectMapper;

	public ExampleController(ExampleAuthInternalFeignClient exampleAuthInternalFeignClient,
			SecurityProperties securityProperties, ObjectMapper objectMapper) {
		this.exampleAuthInternalFeignClient = exampleAuthInternalFeignClient;
		this.securityProperties = securityProperties;
		this.objectMapper = objectMapper;
	}

	@OperationLog(targetType = "Example", serviceDomain = AuditServiceDomain.EXAMPLE,
			bizModule = ExampleAuditBizModule.EXAMPLE_DEMO, operation = OperationLogKind.QUERY)
	@Operation(summary = "示例：公开路径")
	@GetMapping("/public")
	public Result<String> publicEndpoint() {
		return Result.success("ok-public");
	}

	@AuthenticatedApi
	@Operation(summary = "示例：需认证路径")
	@GetMapping("/authenticated")
	public Result<String> authenticatedEndpoint() {
		return Result.success("ok-authenticated");
	}

	@AuthenticatedApi
	@Operation(summary = "示例：放行路径叠加认证注解")
	@GetMapping("/conflict/permit-path-but-authenticated")
	public Result<String> conflictPermitButAuthenticated() {
		try {
			log.info(objectMapper.writeValueAsString(securityProperties));
		}
		catch (JsonProcessingException ex) {
			log.warn("Failed to serialize security properties", ex);
		}
		return Result.success("ok-conflict-permit-authenticated");
	}

	@PublicApi
	@Operation(summary = "示例：认证路径叠加公开注解")
	@GetMapping("/conflict/secured-path-but-public")
	public Result<String> conflictSecuredButPublic() {
		return Result.success("ok-conflict-secured-public");
	}

	@AuthenticatedApi
	@Operation(summary = "示例：查询个人信息")
	@GetMapping("/me")
	public Result<AuthProfile> me() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Object principal = auth != null ? auth.getPrincipal() : null;
		if (!(principal instanceof AuthProfile profile)) {
			return Result.success(null);
		}
		return Result.success(profile);
	}

	@Operation(summary = "示例：判断权限")
	@PreAuthorize("@auth.decide('sys:xxx')")
	@GetMapping("/pre-authorize-1")
	public Result<String> preAuthorize1() {
		return Result.success("ok-pre-authorize");
	}

	@Operation(summary = "示例：判断权限二")
	@PreAuthorize("@auth.decide('sys:user:add')")
	@GetMapping("/pre-authorize-2")
	public Result<String> preAuthorize2() {
		return Result.success("ok-pre-authorize");
	}

	@AuthenticatedApi
	@Operation(summary = "示例：刷新令牌")
	@PostMapping("/trigger-feign")
	public Result<Object> triggerFeign() {
		return exampleAuthInternalFeignClient.refreshToken();
	}

}
