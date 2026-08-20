package com.auth.service.auth.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.auth.model.constants.AuthAuditBizModule;
import com.auth.service.auth.model.query.OnlineUserPageQuery;
import com.auth.service.auth.model.vo.OnlineUserPageVO;
import com.auth.service.auth.model.vo.UserSessionVO;
import com.auth.service.auth.service.SessionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端会话
 *
 * @author Bunny
 */
@Tag(name = "管理端会话", description = "踢人、会话管理、在线用户查询")
@RequiredArgsConstructor
@RequestMapping("/api/auth/admin/users")
@RestController
public class AdminSessionController {

	private final SessionManagementService sessionManagementService;

	@OperationLog(targetType = "ONLINE_USER_LIST", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.QUERY)
	@Operation(summary = "分页查询在线用户")
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/online")
	public Result<PageResponse<OnlineUserPageVO>> pageOnlineUsers(OnlineUserPageQuery query) {
		PageResponse<OnlineUserPageVO> page = sessionManagementService.getOnlineUserPage(query);
		return Result.success(page);
	}

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "踢出指定会话", description = "按 sessionId 或 jti")
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("{userId}/sessions/{sessionId}/kick")
	public Result<Void> kick(@PathVariable("userId") long userId, @PathVariable("sessionId") String sessionId) {
		OperationLogContext.setTargetId(userId);
		sessionManagementService.kickSession(userId, sessionId);

		return Result.success();
	}

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "踢出用户全部会话")
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("{userId}/sessions/kick-all")
	public Result<Void> kickAll(@PathVariable("userId") long userId) {
		OperationLogContext.setTargetId(userId);
		sessionManagementService.kickAllSessions(userId);

		return Result.success();
	}

	@OperationLog(targetType = "USER_SESSION_LIST", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.DELETE)
	@Operation(summary = "批量踢出用户全部会话")
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		sessionManagementService.kickAllSessions(userIds);
		return Result.success();
	}

	@OperationLog(targetType = "USER_SESSION", serviceDomain = AuditServiceDomain.AUTH,
			bizModule = AuthAuditBizModule.AUTH_SESSION, operation = OperationLogKind.QUERY)
	@Operation(summary = "查询用户活跃会话列表")
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("{userId}/sessions")
	public Result<List<UserSessionVO>> getListSessions(@PathVariable("userId") long userId) {
		OperationLogContext.setTargetId(userId);
		List<UserSessionVO> voList = sessionManagementService.listActiveSessions(userId);

		return Result.success(voList);
	}

}
