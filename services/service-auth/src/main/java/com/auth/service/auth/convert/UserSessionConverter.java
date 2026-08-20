package com.auth.service.auth.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.model.vo.UserSessionVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理端用户会话：contract 索引 → API VO（不含 refreshTokenHash）
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface UserSessionConverter {

	UserSessionConverter INSTANCE = Mappers.getMapper(UserSessionConverter.class);

	/**
	 * 会话索引 → 管理端 VO
	 * @param index 会话索引
	 * @return API VO
	 */
	UserSessionVO toVo(UserSessionIndex index);

	/**
	 * 批量转换
	 * @param indexes 会话索引列表
	 * @return API VO 列表
	 */
	List<UserSessionVO> toVoList(List<UserSessionIndex> indexes);

}
