package com.auth.service.system.admin.mapper.admin.role;

import com.auth.service.system.admin.model.entity.SysRolePermissionEntity;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermissionEntity> {

	/**
	 * 按角色 ID 删除全部权限关联
	 * @param roleId 角色 ID
	 */
	@Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
	void deleteByRoleId(@Param("roleId") Long roleId);

	/**
	 * 查询角色已分配权限
	 * @param roleId 角色 ID
	 * @return 已分配权限列表
	 */
	List<PermissionReferencePO> selectAssignedPermissionsByRoleId(@Param("roleId") Long roleId);

	/**
	 * 按权限 ID 查询已绑定角色编码（删除权限前失效快照）
	 * @param permissionId 权限 ID
	 * @return 角色编码列表
	 */
	List<String> selectRoleCodesByPermissionId(@Param("permissionId") Long permissionId);

	/**
	 * 批量插入角色权限关联
	 * @param roleId 角色 ID
	 * @param permissionIds 权限 ID 列表
	 * @param grantorId 授权人 ID
	 * @param createdBy 创建人 ID
	 * @return 插入行数
	 */
	int batchInsertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds,
			@Param("grantorId") Long grantorId, @Param("createdBy") Long createdBy);

}
