package com.auth.service.system.common.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.system.common.service.AuditUserDisplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 内部-审计展示名
 *
 * @author Bunny
 */
@Tag(name = "内部-审计展示名", description = "按用户 ID 批量解析用户名")
@RequestMapping("/api/system/inner/audit-user")
@RestController
public class InternalAuditUserDisplayController {

	private final AuditUserDisplayService auditUserDisplayService;

	public InternalAuditUserDisplayController(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	@InternalApi
	@Operation(summary = "批量解析用户名")
	@PostMapping("/usernames")
	public Result<Map<Long, String>> mapUsernames(@RequestBody List<Long> userIds) {
		Map<Long, String> map = auditUserDisplayService.mapUsernamesByIds(userIds);
		return Result.success(map);
	}

}
