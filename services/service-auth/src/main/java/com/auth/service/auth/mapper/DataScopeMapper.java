package com.auth.service.auth.mapper;

import com.auth.service.auth.model.po.scope.RoleScopePO;
import com.auth.service.auth.model.po.scope.UserScopeByUserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据范围
 *
 * @author Bunny
 */
@Mapper
public interface DataScopeMapper {

	/**
	 * 按用户 ID 批量查询 user_scope
	 * @param userIds 用户 ID 列表，非空
	 * @return 用户范围行
	 */
	List<UserScopeByUserPO> selectByUserIds(@Param("userIds") List<Long> userIds);

	/**
	 * 按角色码列表查询数据范围（每角色至多一行）
	 * @param roleCodes 角色码列表；空列表时返回空结果
	 * @return 角色范围列表
	 */
	List<RoleScopePO> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);

}
