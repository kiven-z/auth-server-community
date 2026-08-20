package com.auth.service.system.admin.mapper.admin.menu;

import com.auth.service.system.admin.model.entity.SysMenuRoleEntity;
import com.auth.service.system.admin.model.po.menu.SysMenuRoleLinkRowPO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单角色关联 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysMenuRoleMapper extends BaseMapper<SysMenuRoleEntity> {

	/**
	 * 查询菜单已分配角色（联表获取角色名称）
	 * @param menuId 菜单ID
	 * @return 已分配角色 DTO 列表
	 */
	List<RoleReferencePO> selectAssignedRolesByMenuId(@Param("menuId") Long menuId);

	/**
	 * 批量查询菜单下已启用、未删除的角色关联
	 * @param menuIds 菜单 ID 列表
	 * @return 关联行
	 */
	List<SysMenuRoleLinkRowPO> selectActiveRoleLinksByMenuIds(@Param("menuIds") List<Long> menuIds);

	/**
	 * 根据菜单ID删除所有角色关联
	 * @param menuId 菜单ID
	 */
	@Delete("DELETE FROM sys_menu_role WHERE menu_id = #{menuId}")
	void deleteByMenuId(@Param("menuId") Long menuId);

	/**
	 * 批量插入菜单-角色关联
	 * @param menuId 菜单ID
	 * @param roleIds 角色 ID 列表
	 * @return 插入行数
	 */
	int batchInsertMenuRoles(@Param("menuId") Long menuId, @Param("roleIds") List<Long> roleIds);

}
