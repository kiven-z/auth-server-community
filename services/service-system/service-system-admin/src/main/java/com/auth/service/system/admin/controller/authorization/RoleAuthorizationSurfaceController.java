package com.auth.service.system.admin.controller.authorization;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.grant.GrantTableSubjectType;
import com.auth.service.system.admin.model.query.authorization.RoleGrantSubjectPageQuery;
import com.auth.service.system.admin.model.query.authorization.RoleMenuPageQuery;
import com.auth.service.system.admin.model.query.authorization.RolePermissionPageQuery;
import com.auth.service.system.admin.model.vo.authorization.GrantSubjectItemRow;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.service.authorization.query.RoleAuthorizationSurfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色授权面
 *
 * @author Bunny
 */
@Tag(name = "角色授权面", description = "绑定权限菜单与授权对象分页")
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/system/role")
@RestController
public class RoleAuthorizationSurfaceController {

	private final RoleAuthorizationSurfaceService roleAuthorizationSurfaceService;

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

	@Operation(summary = "分页查询角色授权对象", description = "按 subjectType 分页；替换原全量 grant-subjects")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/grant-subjects/page")
	public Result<PageResponse<GrantSubjectItemRow>> pageGrantSubjects(@PathVariable("roleId") Long roleId,
			@Validated RoleGrantSubjectPageQuery query) {
		PageResponse<GrantSubjectItemRow> response = roleAuthorizationSurfaceService.pageGrantSubjects(roleId, query);
		return Result.success(response);
	}

	@Operation(summary = "查询角色授权对象种子", description = "Assign 灌种专用：按 subjectType 返回 id+label；无授权为空数组")
	@PreAuthorize("@auth.decide('sys:role:query')")
	@GetMapping("/{roleId}/grant-subjects/ids")
	public Result<List<GrantSubjectItemRow>> listGrantSubjectSeeds(@PathVariable("roleId") Long roleId,
			@RequestParam("subjectType") @NotNull(message = "主体类型不能为空") GrantTableSubjectType subjectType) {
		List<GrantSubjectItemRow> seeds = roleAuthorizationSurfaceService.listGrantSubjectSeeds(roleId, subjectType);
		return Result.success(seeds);
	}

}
