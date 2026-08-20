package com.auth.service.system.message.model.value.delivery;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 渠道投递结果回写参数
 *
 * @author Bunny
 */
@Getter
@Setter
@Accessors(chain = true)
public class ChannelDeliveryResultUpdate {

	/**
	 * 投递目标
	 */
	private String targetValue;

	/**
	 * 目标状态
	 */
	private String status;

	/**
	 * 厂商回执 ID
	 */
	private String providerMsgId;

	/**
	 * 错误码
	 */
	private String errorCode;

	/**
	 * 错误信息
	 */
	private String errorMessage;

	/**
	 * 实际发送时间
	 */
	private Instant sentAt;

}
