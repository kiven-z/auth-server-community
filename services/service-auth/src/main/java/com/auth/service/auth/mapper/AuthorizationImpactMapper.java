package com.auth.service.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 授权影响面
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationImpactMapper {

	/**
	 * 按角色码反查可能持有该角色的用户 ID 经 grant_table 与 v_user_grant_subjects 展开
	 * @param roleCodes 角色编码列表，非空
	 * @return 用户 ID 列表（可能含重复，由上层去重）
	 */
	List<Long> selectUserIdsByRoleCodes(@Param("roleCodes") List<String> roleCodes);

	/**
	 * 按授权主体反查成员用户 ID。成员来自 v_user_grant_subjects
	 * @param subjectType 主体类型名
	 * @param subjectIds 主体 ID 列表，非空
	 * @return 用户 ID 列表
	 */
	List<Long> selectUserIdsByGrantSubjects(@Param("subjectType") String subjectType,
			@Param("subjectIds") List<Long> subjectIds);

	/**
	 * 按权限码桥接查询关联角色码
	 * @param permissionCodes 权限码列表，非空
	 * @return 角色码列表
	 */
	List<String> selectRoleCodesByPermissionCodes(@Param("permissionCodes") List<String> permissionCodes);

}
