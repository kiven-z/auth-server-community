package com.auth.service.auth.mapper;

import com.auth.service.auth.model.po.authorization.UserGrantCodeRowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色权限 Mapper（认证专用）
 *
 * @author Bunny
 */
@Mapper
public interface UserAuthorizationGrantMapper {

	/**
	 * 批量查询用户角色码行
	 * @param userIds 用户 ID 列表，非空
	 * @return 用户角色码行
	 */
	List<UserGrantCodeRowPO> selectRoleRowsByUserIds(@Param("userIds") List<Long> userIds);

	/**
	 * 批量查询用户权限码行
	 * @param userIds 用户 ID 列表，非空
	 * @return 用户权限码行
	 */
	List<UserGrantCodeRowPO> selectPermissionRowsByUserIds(@Param("userIds") List<Long> userIds);

}
