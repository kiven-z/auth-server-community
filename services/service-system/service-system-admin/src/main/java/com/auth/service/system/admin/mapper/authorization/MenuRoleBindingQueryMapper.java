package com.auth.service.system.admin.mapper.authorization;

import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.RoleMenuPageQuery;
import com.auth.service.system.admin.model.query.authorization.SubjectRolePageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 菜单-角色绑定只读查询
 *
 * @author Bunny
 */
@Mapper
public interface MenuRoleBindingQueryMapper {

	/**
	 * 统计角色已绑定菜单数
	 * @param roleId 角色 ID
	 * @param query 过滤条件
	 * @return 菜单数
	 */
	long countMenusByRoleId(@Param("roleId") Long roleId, @Param("query") RoleMenuPageQuery query);

	/**
	 * 分页查询角色已绑定菜单
	 * @param page 分页参数
	 * @param roleId 角色 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<SysPermissionBoundMenuPO> selectMenusByRoleIdPage(@Param("page") Page<?> page, @Param("roleId") Long roleId,
			@Param("query") RoleMenuPageQuery query);

	/**
	 * 统计菜单已绑定角色数
	 * @param menuId 菜单 ID
	 * @param query 过滤条件
	 * @return 角色数
	 */
	long countRolesByMenuId(@Param("menuId") Long menuId, @Param("query") SubjectRolePageQuery query);

	/**
	 * 分页查询菜单已绑定角色
	 * @param page 分页参数
	 * @param menuId 菜单 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<RoleReferencePO> selectRolesByMenuIdPage(@Param("page") Page<?> page, @Param("menuId") Long menuId,
			@Param("query") SubjectRolePageQuery query);

}
