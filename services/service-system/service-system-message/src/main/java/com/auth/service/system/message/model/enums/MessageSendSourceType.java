package com.auth.service.system.message.model.enums;

/**
 * 消息发送任务来源
 *
 * @author Bunny
 */
public enum MessageSendSourceType {

	/**
	 * 管理端手写正文
	 */
	ADMIN_COMPOSE,

	/**
	 * 模板发送
	 */
	TEMPLATE,

	/**
	 * 系统触发
	 */
	SYSTEM

}
