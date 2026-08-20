package com.auth.service.system.message.model.constants;

import lombok.experimental.UtilityClass;

/**
 * message 模块操作审计
 *
 * @author Bunny
 */
@UtilityClass
public class MessageAuditBizModule {

	/**
	 * 消息模板
	 */
	public static final String SYS_MESSAGE_TEMPLATE = "SYS_MESSAGE_TEMPLATE";

	/**
	 * 站内信发送
	 */
	public static final String SYS_IN_APP_MESSAGE = "SYS_IN_APP_MESSAGE";

	/**
	 * 渠道投递记录
	 */
	public static final String SYS_MESSAGE_CHANNEL_DELIVERY = "SYS_MESSAGE_CHANNEL_DELIVERY";

}
