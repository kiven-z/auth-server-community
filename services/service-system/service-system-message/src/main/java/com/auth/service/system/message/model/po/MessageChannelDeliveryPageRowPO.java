package com.auth.service.system.message.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 渠道投递记录分页行
 *
 * @author Bunny
 */
@Schema(name = "MessageChannelDeliveryPageRowPO", title = "渠道投递记录分页行")
@Getter
@Setter
@ToString
public class MessageChannelDeliveryPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "任务/批次 ID")
	private Long taskId;

	@Schema(title = "消息渠道")
	private String channel;

	@Schema(title = "投递状态")
	private String status;

	@Schema(title = "厂商消息标识")
	private String providerMsgId;

	@Schema(title = "失败错误码")
	private String errorCode;

	@Schema(title = "实际发送时间")
	private Instant sentAt;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
