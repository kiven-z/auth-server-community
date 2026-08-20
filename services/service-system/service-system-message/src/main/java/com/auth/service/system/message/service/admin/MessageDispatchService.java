package com.auth.service.system.message.service.admin;

import com.auth.module.message.api.command.TemplateMessageCommand;

/**
 * 消息发送编排服务
 *
 * @author Bunny
 */
public interface MessageDispatchService {

	/**
	 * 按模板发送消息（自动路由渠道）
	 * @param command 模板化发送命令
	 */
	void sendByTemplate(TemplateMessageCommand command);

}
