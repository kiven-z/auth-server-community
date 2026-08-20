package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 渠道投递记录分页查询
 *
 * @author Bunny
 */
@Schema(name = "MessageChannelDeliveryQuery", title = "渠道投递记录查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MessageChannelDeliveryQuery extends PageQueryRequest {

	@Schema(name = "taskId", title = "任务/批次 ID")
	private Long taskId;

	@Schema(name = "channel", title = "消息渠道")
	private String channel;

	@Schema(name = "status", title = "投递状态")
	private String status;

}
