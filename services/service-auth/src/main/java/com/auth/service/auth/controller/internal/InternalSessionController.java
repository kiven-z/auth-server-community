package com.auth.service.auth.controller.internal;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.auth.model.vo.UserSessionVO;
import com.auth.service.auth.service.SessionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内部会话
 *
 * @author Bunny
 */
@Tag(name = "内部会话", description = "服务间会话撤销与查询")
@RequiredArgsConstructor
@RequestMapping("/api/auth/inner/sessions")
@RestController
public class InternalSessionController {

	private final SessionManagementService sessionManagementService;

	@InternalApi
	@Operation(summary = "批量踢出用户全部会话")
	@PostMapping("/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		sessionManagementService.kickAllSessions(userIds);
		return Result.success();
	}

	@InternalApi
	@Operation(summary = "查询用户活跃会话列表")
	@GetMapping("/users/{userId}/sessions")
	public Result<List<UserSessionVO>> listActiveSessions(@PathVariable("userId") long userId) {
		List<UserSessionVO> data = sessionManagementService.listActiveSessions(userId);
		return Result.success(data);
	}

	@InternalApi
	@Operation(summary = "踢出用户指定会话")
	@PostMapping("/users/{userId}/sessions/{sessionId}/kick")
	public Result<Void> kickSession(@PathVariable("userId") long userId, @PathVariable("sessionId") String sessionId) {
		sessionManagementService.kickSession(userId, sessionId);
		return Result.success();
	}

}
