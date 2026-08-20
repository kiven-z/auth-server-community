package com.auth.service.system.admin.mapper.authorization;

import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * grant_table 绑定只读查询
 *
 * @author Bunny
 */
@Mapper
public interface GrantBindingQueryMapper {

	/**
	 * 统计主体已授角色数
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @param query 过滤条件
	 * @return 角色数
	 */
	long countBoundRolesBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId,
			@Param("query") SubjectRolePageQuery query);

	/**
	 * 分页查询主体已授角色
	 * @param page 分页参数
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<RoleReferencePO> selectBoundRolesBySubjectPage(@Param("page") Page<?> page,
			@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId,
			@Param("query") SubjectRolePageQuery query);

	/**
	 * 统计角色下指定类型授权主体数
	 * @param roleId 角色 ID
	 * @param subjectType 主体类型
	 * @return 授权主体数
	 */
	long countSubjectsByRoleIdAndType(@Param("roleId") Long roleId, @Param("subjectType") String subjectType);

}
