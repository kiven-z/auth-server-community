package com.auth.service.system.admin.service.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.PostUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.PostAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.ext.PostBoundUserReferenceVO;

/**
 * 岗位授权面只读服务
 *
 * @author Bunny
 */
public interface PostAuthorizationSurfaceService {

	/**
	 * 分页查询岗位绑定用户（基表，含岗位已停用）
	 * @param postId 岗位 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<PostBoundUserReferenceVO> pageUsers(Long postId, PostUserPageQuery query);

	/**
	 * 查询岗位授权面摘要
	 * @param postId 岗位 ID
	 * @return 授权摘要
	 */
	PostAuthorizationSummaryVO getAuthorizationSummary(Long postId);

}
