package com.auth.service.system.admin.mapper.authorization;

import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.authorization.UserEffectivePermissionPageQuery;
import com.auth.service.system.admin.model.query.authorization.UserEffectiveRolePageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户生效授权只读查询
 *
 * @author Bunny
 */
@Mapper
public interface UserEffectiveAuthorizationQueryMapper {

	/**
	 * 统计用户关联部门数（与 user-dept 分页无过滤条件同源）
	 * @param userId 用户 ID
	 * @return 部门关联数
	 */
	long countDeptsByUserId(@Param("userId") Long userId);

	/**
	 * 统计用户关联岗位数（与 user-post 分页无过滤条件同源）
	 * @param userId 用户 ID
	 * @return 岗位关联数
	 */
	long countPostsByUserId(@Param("userId") Long userId);

	/**
	 * 统计用户生效角色数
	 * @param userId 用户 ID
	 * @param query 过滤条件
	 * @return 生效角色数
	 */
	long countEffectiveRolesByUserId(@Param("userId") Long userId, @Param("query") UserEffectiveRolePageQuery query);

	/**
	 * 分页查询用户生效角色
	 * @param page 分页参数
	 * @param userId 用户 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<RoleReferencePO> selectEffectiveRolesByUserIdPage(@Param("page") Page<?> page, @Param("userId") Long userId,
			@Param("query") UserEffectiveRolePageQuery query);

	/**
	 * 统计用户生效权限数
	 * @param userId 用户 ID
	 * @param query 过滤条件
	 * @return 生效权限数
	 */
	long countEffectivePermissionsByUserId(@Param("userId") Long userId,
			@Param("query") UserEffectivePermissionPageQuery query);

	/**
	 * 分页查询用户生效权限
	 * @param page 分页参数
	 * @param userId 用户 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<PermissionReferencePO> selectEffectivePermissionsByUserIdPage(@Param("page") Page<?> page,
			@Param("userId") Long userId, @Param("query") UserEffectivePermissionPageQuery query);

}
