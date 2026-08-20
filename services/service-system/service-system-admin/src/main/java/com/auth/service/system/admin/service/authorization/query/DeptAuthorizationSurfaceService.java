package com.auth.service.system.admin.service.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.DeptPostPageQuery;
import com.auth.service.system.admin.model.query.authorization.DeptUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.DeptAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.PostReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.DeptBoundUserReferenceVO;

/**
 * 部门授权面只读服务
 *
 * @author Bunny
 */
public interface DeptAuthorizationSurfaceService {

	/**
	 * 分页查询部门关联用户
	 * @param deptId 部门 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<DeptBoundUserReferenceVO> pageUsers(Long deptId, DeptUserPageQuery query);

	/**
	 * 分页查询部门下属岗位
	 * @param deptId 部门 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<PostReferenceVO> pagePosts(Long deptId, DeptPostPageQuery query);

	/**
	 * 查询部门授权面摘要
	 * @param deptId 部门 ID
	 * @return 授权摘要
	 */
	DeptAuthorizationSummaryVO getAuthorizationSummary(Long deptId);

}
