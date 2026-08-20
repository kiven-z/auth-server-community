package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.SysRolePermissionEntity;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色权限分配服务
 *
 * @author Bunny
 */
public interface SysRolePermissionService extends IService<SysRolePermissionEntity> {

	/**
	 * 查询角色已分配权限
	 * @param roleId 角色 ID
	 * @return 已分配权限行列表
	 */
	List<PermissionReferenceVO> listAssignedPermissions(Long roleId);

	/**
	 * 全量覆盖角色权限（写后触发授权失效）
	 * @param roleId 角色 ID
	 * @param permissionIds 权限 ID 列表（可为空表示清空）
	 */
	void assignPermissions(Long roleId, List<Long> permissionIds);

}
