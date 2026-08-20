package com.auth.service.auth.model.value.login;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 刷新令牌业务结果：新令牌对 + 会话级「记住我」标记 + 当前授权画像
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class RefreshSessionResult {

	/**
	 * 访问/刷新令牌对
	 */
	@NonNull
	TokenPair tokenPair;

	/**
	 * 登录时是否勾选记住我
	 */
	boolean rememberMe;

	/**
	 * 当前 Redis 授权画像（用于下发 roles/permissions）
	 */
	@NonNull
	AuthProfile authProfile;

}
