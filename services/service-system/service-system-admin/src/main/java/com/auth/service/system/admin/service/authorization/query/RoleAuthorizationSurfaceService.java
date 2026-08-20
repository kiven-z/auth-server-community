package com.auth.service.system.admin.service.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.RoleMenuPageQuery;
import com.auth.service.system.admin.model.query.authorization.RolePermissionPageQuery;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;

/**
 * 角色授权面只读服务
 *
 * @author Bunny
 */
public interface RoleAuthorizationSurfaceService {

	/**
	 * 查询角色授权面摘要
	 * @param roleId 角色 ID
	 * @return 关系计数摘要
	 */
	RoleAuthorizationSummaryVO getAuthorizationSummary(Long roleId);

	/**
	 * 分页查询角色已绑定权限
	 * @param roleId 角色 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<PermissionReferenceVO> pagePermissions(Long roleId, RolePermissionPageQuery query);

	/**
	 * 分页查询角色已绑定菜单
	 * @param roleId 角色 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysPermissionBoundMenuItemVO> pageMenus(Long roleId, RoleMenuPageQuery query);

}
