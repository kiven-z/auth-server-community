package com.auth.service.system.admin.mapper.authorization;

import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.RolePermissionPageQuery;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色-权限绑定只读查询
 *
 * @author Bunny
 */
@Mapper
public interface RolePermissionBindingQueryMapper {

	/**
	 * 统计角色已绑定权限数
	 * @param roleId 角色 ID
	 * @param query 过滤条件
	 * @return 权限数
	 */
	long countPermissionsByRoleId(@Param("roleId") Long roleId, @Param("query") RolePermissionPageQuery query);

	/**
	 * 分页查询角色已绑定权限
	 * @param page 分页参数
	 * @param roleId 角色 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<PermissionReferencePO> selectPermissionsByRoleIdPage(@Param("page") Page<?> page,
			@Param("roleId") Long roleId, @Param("query") RolePermissionPageQuery query);

	/**
	 * 统计权限已绑定角色数
	 * @param permissionId 权限 ID
	 * @param query 过滤条件
	 * @return 角色数
	 */
	long countRolesByPermissionId(@Param("permissionId") Long permissionId, @Param("query") SubjectRolePageQuery query);

	/**
	 * 分页查询权限已绑定角色
	 * @param page 分页参数
	 * @param permissionId 权限 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<RoleReferencePO> selectRolesByPermissionIdPage(@Param("page") Page<?> page,
			@Param("permissionId") Long permissionId, @Param("query") SubjectRolePageQuery query);

}
