package com.auth.module.message.api.channel;

import com.auth.module.message.api.command.TemplateMessageCommand;

/**
 * 消息发送渠道 SPI：新增渠道时实现本接口并注册为 Spring Bean。
 *
 * @author Bunny
 */
public interface MessageSender {

	/**
	 * 本实现支持的渠道
	 * @return 渠道枚举
	 */
	MessageChannel channel();

	/**
	 * 按模板发送消息
	 * @param command 模板化发送命令
	 */
	void sendByTemplate(TemplateMessageCommand command);

}
