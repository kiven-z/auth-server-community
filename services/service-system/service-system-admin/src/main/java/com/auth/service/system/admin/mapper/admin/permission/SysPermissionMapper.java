package com.auth.service.system.admin.mapper.admin.permission;

import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统权限 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionEntity> {

	/**
	 * 分页查询权限（含创建人、更新人用户名展示列）
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段（仅列名与 ASC/DESC）
	 * @return 分页结果
	 */
	IPage<SysPermissionEntity> selectListByPage(@Param("page") Page<SysPermissionEntity> page,
			@Param("query") SysPermissionQuery query, @Param("orderBySql") String orderBySql);

	/**
	 * 按权限编码批量查询权限关联投影
	 * @param permissionCodes 权限编码列表
	 * @return 权限关联投影列表
	 */
	List<PermissionReferencePO> selectReferenceByPermissionCodes(
			@Param("permissionCodes") List<String> permissionCodes);

	/**
	 * 批量查询存在且已启用的权限 ID
	 * @param permissionIds 权限 ID 列表（调用方应已去重）
	 * @return 符合条件的权限 ID
	 */
	List<Long> selectActivePermissionIds(@Param("permissionIds") List<Long> permissionIds);

}
