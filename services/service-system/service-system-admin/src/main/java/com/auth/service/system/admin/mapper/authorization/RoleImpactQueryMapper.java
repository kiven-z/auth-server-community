package com.auth.service.system.admin.mapper.authorization;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.system.admin.model.po.reference.UserReferencePO;
import com.auth.service.system.admin.model.query.authorization.RoleImpactUserPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 角色影响用户只读查询
 *
 * @author Bunny
 */
@Mapper
public interface RoleImpactQueryMapper {

	/**
	 * 分页查询角色影响用户
	 * @param page 分页参数
	 * @param roleId 角色 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<UserReferencePO> selectImpactUsersByRoleIdPage(@Param("page") Page<?> page, @Param("roleId") Long roleId,
			@Param("query") RoleImpactUserPageQuery query);

	/**
	 * 列出角色全部影响用户 ID（刷新画像）
	 * @param roleId 角色 ID
	 * @return 去重用户 ID
	 */
	List<Long> selectImpactUserIdsByRoleId(@Param("roleId") Long roleId);

}
