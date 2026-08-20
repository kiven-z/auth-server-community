package com.auth.module.message.api.port;

import com.auth.module.message.api.command.TemplateMessageCommand;

/**
 * 跨模块消息发送能力（进程内集成契约）
 *
 * @author Bunny
 */
public interface MessageSendOperations {

	/**
	 * 按模板发送消息（自动路由渠道）
	 * @param command 模板化发送命令
	 */
	void sendByTemplate(TemplateMessageCommand command);

}
