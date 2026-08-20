package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;

import java.util.List;

/**
 * 用户直连角色授权服务
 *
 * @author Bunny
 */
public interface SysUserRoleService {

	/**
	 * 查询用户已分配直连角色
	 * @param userId 用户 ID
	 * @return 已分配角色回显列表
	 */
	List<RoleReferenceVO> listAssignedRoles(Long userId);

	/**
	 * 全量覆盖用户直连角色授权
	 * @param userId 用户 ID
	 * @param form 角色编码列表
	 */
	void replaceUserRoles(Long userId, GrantTableAssignRoleForm form);

}
