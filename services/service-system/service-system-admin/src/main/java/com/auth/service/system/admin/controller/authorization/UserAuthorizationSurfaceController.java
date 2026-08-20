package com.auth.service.system.admin.controller.authorization;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.UserEffectivePermissionPageQuery;
import com.auth.service.system.admin.model.query.authorization.UserEffectiveRolePageQuery;
import com.auth.service.system.admin.model.vo.authorization.UserAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.authorization.query.UserAuthorizationSurfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户授权面
 *
 * @author Bunny
 */
@Tag(name = "用户授权面", description = "直连角色 / 生效角色权限分页与授权摘要")
@RequiredArgsConstructor
@RequestMapping("/api/system/user")
@RestController
public class UserAuthorizationSurfaceController {

	private final UserAuthorizationSurfaceService userAuthorizationSurfaceService;

	@Operation(summary = "分页查询用户生效角色")
	@PreAuthorize("@auth.decide('sys:user:query')")
	@GetMapping("/{userId}/effective-roles/page")
	public Result<PageResponse<RoleReferenceVO>> pageEffectiveRoles(@PathVariable("userId") Long userId,
			UserEffectiveRolePageQuery query) {
		PageResponse<RoleReferenceVO> response = userAuthorizationSurfaceService.pageEffectiveRoles(userId, query);
		return Result.success(response);
	}

	@Operation(summary = "分页查询用户生效权限")
	@PreAuthorize("@auth.decide('sys:user:query')")
	@GetMapping("/{userId}/effective-permissions/page")
	public Result<PageResponse<PermissionReferenceVO>> pageEffectivePermissions(@PathVariable("userId") Long userId,
			UserEffectivePermissionPageQuery query) {
		PageResponse<PermissionReferenceVO> response = userAuthorizationSurfaceService.pageEffectivePermissions(userId,
				query);
		return Result.success(response);
	}

	@Operation(summary = "查询用户授权面摘要")
	@PreAuthorize("@auth.decide('sys:user:query')")
	@GetMapping("/{userId}/authorization-summary")
	public Result<UserAuthorizationSummaryVO> authorizationSummary(@PathVariable("userId") Long userId) {
		UserAuthorizationSummaryVO summary = userAuthorizationSurfaceService.getAuthorizationSummary(userId);
		return Result.success(summary);
	}

}
