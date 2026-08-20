package com.auth.service.auth.model.value.login;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 登录成功后的用户身份快照
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class AuthenticatedUser {

	/**
	 * 用户 ID
	 */
	Long id;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 从登录账户构建身份快照
	 * @param account 登录账户
	 * @return 认证用户快照
	 */
	public static AuthenticatedUser from(LoginAccount account) {
		return AuthenticatedUser.builder().id(account.id()).username(account.username()).build();
	}

}
