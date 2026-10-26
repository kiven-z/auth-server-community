package com.auth.service.system.admin.service.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.grant.GrantTableSubjectType;
import com.auth.service.system.admin.model.query.authorization.RoleGrantSubjectPageQuery;
import com.auth.service.system.admin.model.query.authorization.RoleMenuPageQuery;
import com.auth.service.system.admin.model.query.authorization.RolePermissionPageQuery;
import com.auth.service.system.admin.model.vo.authorization.GrantSubjectItemRow;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;

import java.util.List;

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

	/**
	 * 分页查询角色授权对象
	 * @param roleId 角色 ID
	 * @param query 查询条件（含主体类型）
	 * @return 分页数据
	 */
	PageResponse<GrantSubjectItemRow> pageGrantSubjects(Long roleId, RoleGrantSubjectPageQuery query);

	/**
	 * 查询角色授权对象种子（Assign 灌种，id + label）
	 * @param roleId 角色 ID
	 * @param subjectType 主体类型
	 * @return 种子行；无授权时为空列表
	 */
	List<GrantSubjectItemRow> listGrantSubjectSeeds(Long roleId, GrantTableSubjectType subjectType);

}
