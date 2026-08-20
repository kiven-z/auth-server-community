package com.auth.service.auth.mapper;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper（auth 服务共用）
 *
 * @author Bunny
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

	/**
	 * 按凭证维度查询用户
	 * @param dimension 凭证维度（列白名单）
	 * @param value 凭证值
	 * @param activeOnly 是否仅查询正常状态用户
	 * @return 用户信息，不存在时为 null
	 */
	UserEntity selectByCredential(@Param("dimension") CredentialDimension dimension, @Param("value") String value,
			@Param("activeOnly") boolean activeOnly);

	/**
	 * 批量查询用户失效分桶所需状态
	 * @param userIds 用户 ID 列表，非空
	 * @return 状态投影
	 */
	List<UserInvalidationStatePO> selectInvalidationStatesByUserIds(@Param("userIds") List<Long> userIds);

	/**
	 * 批量递增 perm_version
	 * @param userIds 用户 ID 列表，非空
	 * @return 更新行数
	 */
	int incrementPermVersionByUserIds(@Param("userIds") List<Long> userIds);

}
