package com.auth.service.system.admin.mapper.authorization;

import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.RoleGrantSubjectPageQuery;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.auth.service.system.admin.model.vo.authorization.GrantSubjectItemRow;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * grant_table 绑定只读查询
 *
 * @author Bunny
 */
@Mapper
public interface GrantBindingQueryMapper {

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
	 * 分页查询角色下指定类型授权对象
	 * @param page 分页参数
	 * @param roleId 角色 ID
	 * @param subjectType 主体类型
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<GrantSubjectItemRow> selectSubjectsByRoleIdAndTypePage(@Param("page") Page<?> page,
			@Param("roleId") Long roleId, @Param("subjectType") String subjectType,
			@Param("query") RoleGrantSubjectPageQuery query);

	/**
	 * 查询角色下指定类型授权对象种子（id + label，无过滤）
	 * @param roleId 角色 ID
	 * @param subjectType 主体类型
	 * @return 种子行列表
	 */
	List<GrantSubjectItemRow> selectSubjectsByRoleIdAndType(@Param("roleId") Long roleId,
			@Param("subjectType") String subjectType);

}
