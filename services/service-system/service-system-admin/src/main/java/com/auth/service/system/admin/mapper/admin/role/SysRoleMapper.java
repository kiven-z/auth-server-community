package com.auth.service.system.admin.mapper.admin.role;

import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {

	/**
	 * 分页查询角色
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段（仅列名与 ASC/DESC）
	 * @return 分页结果
	 */
	IPage<SysRoleEntity> selectListByPage(@Param("page") Page<SysRoleEntity> page, @Param("query") SysRoleQuery query,
			@Param("orderBySql") String orderBySql);

	/**
	 * 查询角色下拉选项
	 * @param roleName 角色名称（可选模糊）
	 * @param roleCode 角色编码（可选模糊）
	 * @return 角色选项列表
	 */
	List<SysRoleEntity> selectRoleOptions(@Param("roleName") String roleName, @Param("roleCode") String roleCode);

	/**
	 * 按角色编码批量查询角色关联投影
	 * @param roleCodes 角色编码列表
	 * @return 角色关联投影列表
	 */
	List<RoleReferencePO> selectReferenceByRoleCodes(@Param("roleCodes") List<String> roleCodes);

	/**
	 * 批量查询存在且已启用的角色 ID
	 * @param roleIds 角色 ID 列表（调用方应已去重）
	 * @return 符合条件的角色 ID
	 */
	List<Long> selectActiveRoleIds(@Param("roleIds") List<Long> roleIds);

}
