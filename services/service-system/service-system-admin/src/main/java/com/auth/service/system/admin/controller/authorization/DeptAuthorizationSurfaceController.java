package com.auth.service.system.admin.controller.authorization;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.DeptPostPageQuery;
import com.auth.service.system.admin.model.query.authorization.DeptUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.DeptAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.PostReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.DeptBoundUserReferenceVO;
import com.auth.service.system.admin.service.authorization.query.DeptAuthorizationSurfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门授权面
 *
 * @author Bunny
 */
@Tag(name = "部门授权面", description = "关联用户岗位分页与授权摘要")
@RequiredArgsConstructor
@RequestMapping("/api/system/dept")
@RestController
public class DeptAuthorizationSurfaceController {

	private final DeptAuthorizationSurfaceService deptAuthorizationSurfaceService;

	@Operation(summary = "分页查询部门关联用户")
	@PreAuthorize("@auth.decide('sys:dept:query')")
	@GetMapping("/{deptId}/users/page")
	public Result<PageResponse<DeptBoundUserReferenceVO>> pageUsers(@PathVariable("deptId") Long deptId,
			DeptUserPageQuery query) {
		PageResponse<DeptBoundUserReferenceVO> response = deptAuthorizationSurfaceService.pageUsers(deptId, query);
		return Result.success(response);
	}

	@Operation(summary = "分页查询部门下属岗位")
	@PreAuthorize("@auth.decide('sys:dept:query')")
	@GetMapping("/{deptId}/posts/page")
	public Result<PageResponse<PostReferenceVO>> pagePosts(@PathVariable("deptId") Long deptId,
			DeptPostPageQuery query) {
		PageResponse<PostReferenceVO> response = deptAuthorizationSurfaceService.pagePosts(deptId, query);
		return Result.success(response);
	}

	@Operation(summary = "查询部门授权面摘要")
	@PreAuthorize("@auth.decide('sys:dept:query')")
	@GetMapping("/{deptId}/authorization-summary")
	public Result<DeptAuthorizationSummaryVO> authorizationSummary(@PathVariable("deptId") Long deptId) {
		DeptAuthorizationSummaryVO summary = deptAuthorizationSurfaceService.getAuthorizationSummary(deptId);
		return Result.success(summary);
	}

}
