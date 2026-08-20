package com.auth.module.message.api.model.enums.scene;

import com.auth.module.message.api.channel.MessageChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 邮件渠道消息场景
 *
 * @author Bunny
 */
@Getter
@RequiredArgsConstructor
public enum EmailMessageScene {

	/**
	 * 登录邮箱验证码
	 */
	LOGIN_EMAIL("login-email-code", 1);

	/**
	 * 模板/场景编码
	 */
	private final String templateCode;

	/**
	 * 同 scene_code + channel 多条时的权重（越大越高）
	 */
	private final int priority;

	/**
	 * 发送渠道
	 * @return 邮件渠道
	 */
	public MessageChannel getChannel() {
		return MessageChannel.EMAIL;
	}

}
