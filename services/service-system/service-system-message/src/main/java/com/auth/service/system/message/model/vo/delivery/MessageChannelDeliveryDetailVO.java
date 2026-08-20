package com.auth.service.system.message.model.vo.delivery;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 渠道投递记录详情返回对象
 *
 * @author Bunny
 */
@Schema(name = "MessageChannelDeliveryDetailVO", title = "渠道投递记录详情返回对象")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MessageChannelDeliveryDetailVO extends BaseResponse {

	@JsonStringFormat
	@Schema(name = "taskId", title = "任务/批次 ID")
	private Long taskId;

	@Schema(name = "channel", title = "消息渠道")
	private String channel;

	@Schema(name = "targetValue", title = "投递目标")
	private String targetValue;

	@Schema(name = "status", title = "投递状态")
	private String status;

	@Schema(name = "providerMsgId", title = "厂商消息标识")
	private String providerMsgId;

	@Schema(name = "errorCode", title = "失败错误码")
	private String errorCode;

	@Schema(name = "errorMessage", title = "失败原因")
	private String errorMessage;

	@Schema(name = "sentAt", title = "实际发送时间")
	private Instant sentAt;

	@Schema(name = "retryCount", title = "重试次数")
	private Integer retryCount;

	@Schema(name = "remark", title = "备注")
	private String remark;

}
