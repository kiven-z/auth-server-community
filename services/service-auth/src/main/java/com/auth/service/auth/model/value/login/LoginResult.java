package com.auth.service.auth.model.value.login;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 登录结果（领域层）：认证用户快照 + 授权画像，供应用层组装 API 响应与签发令牌
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class LoginResult {

	/**
	 * 已认证用户展示信息
	 */
	@NotNull
	AuthenticatedUser authenticatedUser;

	/**
	 * 授权画像
	 */
	@NotNull
	AuthProfile authProfile;

	/**
	 * 登录审计类型
	 */
	@NotNull
	AuthLoginLogType loginLogType;

}
