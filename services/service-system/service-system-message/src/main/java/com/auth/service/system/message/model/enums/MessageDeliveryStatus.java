package com.auth.service.system.message.model.enums;

/**
 * 渠道消息投递状态
 *
 * @author Bunny
 */
public enum MessageDeliveryStatus {

	/**
	 * 待发送
	 */
	PENDING,

	/**
	 * 已发送成功
	 */
	SUCCESS,

	/**
	 * 发送失败
	 */
	FAILED,

	/**
	 * 已跳过
	 */
	SKIPPED

}
