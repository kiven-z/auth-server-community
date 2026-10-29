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
	 * 分页查询用户生效角色
	 * @param page 分页参数
	 * @param userId 用户 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<RoleReferencePO> selectEffectiveRolesByUserIdPage(@Param("page") Page<?> page, @Param("userId") Long userId,
			@Param("query") UserEffectiveRolePageQuery query);

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
