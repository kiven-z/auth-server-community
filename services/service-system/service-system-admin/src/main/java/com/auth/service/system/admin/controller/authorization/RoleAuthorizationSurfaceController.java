package com.auth.service.system.admin.controller.authorization;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.RoleMenuPageQuery;
import com.auth.service.system.admin.model.query.authorization.RolePermissionPageQuery;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.service.authorization.query.RoleAuthorizationSurfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色授权面
 *
 * @author Bunny
 */
@Tag(name = "角色授权面", description = "绑定权限菜单分页与授权摘要")
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/system/role")
@RestController
public class RoleAuthorizationSurfaceController {

	private final RoleAuthorizationSurfaceService roleAuthorizationSurfaceService;

	@Operation(summary = "查询角色授权面摘要")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/authorization-summary")
	public Result<RoleAuthorizationSummaryVO> authorizationSummary(@PathVariable("roleId") Long roleId) {
		RoleAuthorizationSummaryVO summary = roleAuthorizationSurfaceService.getAuthorizationSummary(roleId);
		return Result.success(summary);
	}

	@Operation(summary = "分页查询角色已绑定权限")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/permissions/page")
	public Result<PageResponse<PermissionReferenceVO>> pagePermissions(@PathVariable("roleId") Long roleId,
			RolePermissionPageQuery query) {
		PageResponse<PermissionReferenceVO> response = roleAuthorizationSurfaceService.pagePermissions(roleId, query);
		return Result.success(response);
	}

	@Operation(summary = "分页查询角色已绑定菜单")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/menus/page")
	public Result<PageResponse<SysPermissionBoundMenuItemVO>> pageMenus(@PathVariable("roleId") Long roleId,
			RoleMenuPageQuery query) {
		PageResponse<SysPermissionBoundMenuItemVO> response = roleAuthorizationSurfaceService.pageMenus(roleId, query);
		return Result.success(response);
	}

}
