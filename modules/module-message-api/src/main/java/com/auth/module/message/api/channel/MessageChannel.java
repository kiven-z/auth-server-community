package com.auth.module.message.api.channel;

/**
 * 消息发送渠道（逻辑渠道；具体厂商由发送实现 / 配置决定）
 *
 * @author Bunny
 */
public enum MessageChannel {

	/**
	 * 电子邮件
	 */
	EMAIL,

	/**
	 * 短信
	 */
	SMS,

	/**
	 * 钉钉
	 */
	DING_TALK,

	/**
	 * 站内信
	 */
	IN_APP,

}
