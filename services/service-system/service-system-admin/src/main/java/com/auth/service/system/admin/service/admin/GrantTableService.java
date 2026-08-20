package com.auth.service.system.admin.service.admin;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;

import java.util.List;

/**
 * grant_table 读写与主体角色授权编排
 *
 * @author Bunny
 */
public interface GrantTableService {

	/**
	 * 查询主体已分配角色
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 已分配角色回显列表
	 */
	List<RoleReferenceVO> listAssignedRoles(GrantTableSubjectType subjectType, Long subjectId);

	/**
	 * 查询主体已绑定角色
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 已绑定角色回显列表
	 */
	List<RoleReferenceVO> listBoundRoles(GrantTableSubjectType subjectType, Long subjectId);

	/**
	 * 全量覆盖主体角色授权
	 * @param subjectType 主体类型字符串
	 * @param subjectId 主体 ID
	 * @param roleIds 角色 ID 列表
	 */
	void replaceSubjectRoleGrants(String subjectType, Long subjectId, List<Long> roleIds);

}
