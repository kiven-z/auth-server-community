package com.auth.service.system.admin.mapper.admin.role;

import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * grant_table 读写
 *
 * @author Bunny
 */
@Mapper
public interface GrantTableMapper extends BaseMapper<GrantTableEntity> {

	/**
	 * 统计指定角色在 grant_table 中的引用行数
	 * @param roleId 角色 ID
	 * @return 引用行数
	 */
	@Select("SELECT COUNT(1) FROM grant_table WHERE role_id = #{roleId}")
	long countByRoleId(@Param("roleId") Long roleId);

	/**
	 * 按授权主体类型与主体 ID 列表批量删除 grant 行
	 * @param subjectType 主体类型
	 * @param subjectIds 主体 ID 列表
	 * @return 删除行数
	 */
	int deleteBySubjectIds(@Param("subjectType") String subjectType, @Param("subjectIds") List<Long> subjectIds);

	/**
	 * 按授权主体查询已分配角色（管理部门/岗位角色表单当前勾选）
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 已授权角色投影列表
	 */
	List<RoleReferencePO> selectAssignedRolesBySubject(@Param("subjectType") String subjectType,
			@Param("subjectId") Long subjectId);

	/**
	 * 按角色与主体类型查询已绑定主体 ID
	 * @param roleId 角色 ID
	 * @param subjectType 主体类型
	 * @return 主体 ID 列表
	 */
	@Select("SELECT gt.subject_id FROM grant_table gt WHERE gt.role_id = #{roleId} AND gt.subject_type = #{subjectType}")
	List<Long> selectSubjectIdsByRoleIdAndSubjectType(@Param("roleId") Long roleId,
			@Param("subjectType") String subjectType);

	/**
	 * 按角色与主体类型删除 grant 行（不影响该主体对其它角色的授权）
	 * @param roleId 角色 ID
	 * @param subjectType 主体类型
	 * @return 删除行数
	 */
	@Delete("DELETE FROM grant_table WHERE role_id = #{roleId} AND subject_type = #{subjectType}")
	int deleteByRoleIdAndSubjectType(@Param("roleId") Long roleId, @Param("subjectType") String subjectType);

}
