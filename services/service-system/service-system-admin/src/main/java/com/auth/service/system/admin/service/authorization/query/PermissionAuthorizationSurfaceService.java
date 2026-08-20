package com.auth.service.system.admin.service.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;

/**
 * 权限授权面只读服务
 *
 * @author Bunny
 */
public interface PermissionAuthorizationSurfaceService {

	/**
	 * 分页查询权限已绑定角色
	 * @param permissionId 权限 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<RoleReferenceVO> pageRoles(Long permissionId, SubjectRolePageQuery query);

}
