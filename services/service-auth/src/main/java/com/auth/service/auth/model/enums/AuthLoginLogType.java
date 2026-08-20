package com.auth.service.auth.model.enums;

import lombok.Getter;

/**
 * 登录日志类型
 *
 * @author Bunny
 */
@Getter
public enum AuthLoginLogType {

	/**
	 * 邮箱登录
	 */
	LOGIN_EMAIL,

	/**
	 * 密码登录
	 */
	LOGIN_PASSWORD,

	/**
	 * 刷新令牌
	 */
	REFRESH_TOKEN,

	/**
	 * 短信登录
	 */
	LOGIN_SMS,

	/**
	 * 登出
	 */
	LOGOUT,

}
