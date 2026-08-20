package com.auth.service.auth.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.model.value.login.CompletedLoginSession;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 登录会话读模型 → HTTP 响应。
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface LoginSessionConverter {

	LoginSessionConverter INSTANCE = Mappers.getMapper(LoginSessionConverter.class);

	/**
	 * 登录会话读模型 → API 登录响应。
	 * @param session 已签发登录会话
	 * @return HTTP 登录响应
	 */
	UserLoginResponse toUserLoginResponse(CompletedLoginSession session);

}
