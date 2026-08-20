package com.auth.service.auth.model.value.login;

import com.auth.module.platform.persistence.model.UserEntity;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 登录流程中的用户快照
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class LoginAccount {

	/**
	 * 用户 ID
	 */
	Long id;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 账户状态（与平台 sys_user.status 一致）
	 */
	Integer status;

	/**
	 * 加密后的密码（验证码登录场景可为 null）
	 */
	String encodedPassword;

	/**
	 * 权限版本
	 */
	Long permVersion;

	/**
	 * 从持久化用户实体构建登录账户快照。
	 * @param user 用户实体
	 * @return 登录账户
	 */
	public static LoginAccount from(UserEntity user) {
		return LoginAccount.builder()
			.id(user.getId())
			.username(user.getUsername())
			.status(user.getStatus())
			.encodedPassword(user.getPassword())
			.permVersion(user.getPermVersion())
			.build();
	}

}
