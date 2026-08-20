package com.auth.service.system.admin.service.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.query.authorization.UserEffectivePermissionPageQuery;
import com.auth.service.system.admin.model.query.authorization.UserEffectiveRolePageQuery;
import com.auth.service.system.admin.model.vo.authorization.UserAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;

/**
 * 用户授权面只读服务
 *
 * @author Bunny
 */
public interface UserAuthorizationSurfaceService {

	/**
	 * 分页查询用户直连角色
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<RoleReferenceVO> pageDirectRoles(Long userId, SubjectRolePageQuery query);

	/**
	 * 分页查询用户生效角色
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<RoleReferenceVO> pageEffectiveRoles(Long userId, UserEffectiveRolePageQuery query);

	/**
	 * 分页查询用户生效权限
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<PermissionReferenceVO> pageEffectivePermissions(Long userId, UserEffectivePermissionPageQuery query);

	/**
	 * 获取用户授权面摘要
	 * @param userId 用户 ID
	 * @return 授权面摘要
	 */
	UserAuthorizationSummaryVO getAuthorizationSummary(Long userId);

}
