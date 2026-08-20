package com.auth.service.system.message.model.vo.delivery;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 渠道投递记录分页返回对象
 *
 * @author Bunny
 */
@Schema(name = "MessageChannelDeliveryPageVO", title = "渠道投递记录分页返回对象")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MessageChannelDeliveryPageVO extends BaseResponse {

	@JsonStringFormat
	@Schema(name = "taskId", title = "任务/批次 ID")
	private Long taskId;

	@Schema(name = "channel", title = "消息渠道")
	private String channel;

	@Schema(name = "status", title = "投递状态")
	private String status;

	@Schema(name = "providerMsgId", title = "厂商消息标识")
	private String providerMsgId;

	@Schema(name = "errorCode", title = "失败错误码")
	private String errorCode;

	@Schema(name = "sentAt", title = "实际发送时间")
	private Instant sentAt;

}
