package com.auth.service.system.admin.controller.authorization;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.authorization.query.PermissionAuthorizationSurfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限授权面
 *
 * @author Bunny
 */
@Tag(name = "权限授权面", description = "绑定角色分页")
@RequiredArgsConstructor
@RequestMapping("/api/system/permission")
@RestController
public class PermissionAuthorizationSurfaceController {

	private final PermissionAuthorizationSurfaceService permissionAuthorizationSurfaceService;

	@Operation(summary = "分页查询权限已绑定角色")
	@PreAuthorize("@auth.decide('sys:permission:query')")
	@GetMapping("/{permissionId}/roles/page")
	public Result<PageResponse<RoleReferenceVO>> pageRoles(@PathVariable("permissionId") Long permissionId,
			SubjectRolePageQuery query) {
		PageResponse<RoleReferenceVO> response = permissionAuthorizationSurfaceService.pageRoles(permissionId, query);
		return Result.success(response);
	}

}
