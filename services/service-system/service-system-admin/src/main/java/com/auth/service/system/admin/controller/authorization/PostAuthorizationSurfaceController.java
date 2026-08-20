package com.auth.service.system.admin.controller.authorization;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.PostUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.PostAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.ext.PostBoundUserReferenceVO;
import com.auth.service.system.admin.service.authorization.query.PostAuthorizationSurfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位授权面
 *
 * @author Bunny
 */
@Tag(name = "岗位授权面", description = "关联用户分页与授权摘要")
@RequiredArgsConstructor
@RequestMapping("/api/system/post")
@RestController
public class PostAuthorizationSurfaceController {

	private final PostAuthorizationSurfaceService postAuthorizationSurfaceService;

	@Operation(summary = "分页查询岗位关联用户")
	@PreAuthorize("@auth.decide('sys:post:query')")
	@GetMapping("/{postId}/users/page")
	public Result<PageResponse<PostBoundUserReferenceVO>> pageUsers(@PathVariable("postId") Long postId,
			PostUserPageQuery query) {
		PageResponse<PostBoundUserReferenceVO> response = postAuthorizationSurfaceService.pageUsers(postId, query);
		return Result.success(response);
	}

	@Operation(summary = "查询岗位授权面摘要")
	@PreAuthorize("@auth.decide('sys:post:query')")
	@GetMapping("/{postId}/authorization-summary")
	public Result<PostAuthorizationSummaryVO> authorizationSummary(@PathVariable("postId") Long postId) {
		PostAuthorizationSummaryVO summary = postAuthorizationSurfaceService.getAuthorizationSummary(postId);
		return Result.success(summary);
	}

}
