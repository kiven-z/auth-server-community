package com.auth.service.system.message.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 渠道消息投递记录
 *
 * @author Bunny
 */
@TableName("message_channel_delivery")
@Schema(name = "MessageChannelDeliveryEntity", title = "渠道消息投递记录")
@Getter
@Setter
@Accessors(chain = true)
public class MessageChannelDeliveryEntity extends BaseEntity {

	@Schema(title = "任务/批次 ID")
	private Long taskId;

	@Schema(title = "逻辑渠道")
	private String channel;

	@Schema(title = "投递目标")
	private String targetValue;

	@Schema(title = "投递状态")
	private String status;

	@Schema(title = "厂商消息标识")
	private String providerMsgId;

	@Schema(title = "失败错误码")
	private String errorCode;

	@Schema(title = "失败原因")
	private String errorMessage;

	@Schema(title = "实际发送时间")
	private Instant sentAt;

	@Schema(title = "重试次数")
	private Integer retryCount;

}
