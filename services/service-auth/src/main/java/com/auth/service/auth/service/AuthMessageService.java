package com.auth.service.auth.service;

/**
 * 认证消息服务：登录邮箱/短信验证码发送
 *
 * @author Bunny
 */
public interface AuthMessageService {

	/**
	 * 发送登录邮箱验证码
	 * @param email 邮箱
	 */
	void sendEmailCode(String email);

	/**
	 * 发送登录手机短信验证码
	 * @param phone 手机号
	 */
	void sendSmsCode(String phone);

}
