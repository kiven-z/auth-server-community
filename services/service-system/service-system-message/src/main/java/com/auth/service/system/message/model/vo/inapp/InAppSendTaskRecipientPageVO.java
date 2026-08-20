package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 站内信发送任务收件人/互动用户分页行
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskRecipientPageVO", title = "站内信发送任务收件人分页返回对象")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppSendTaskRecipientPageVO extends BaseResponse {

	@JsonStringFormat
	@Schema(name = "messageId", title = "站内信任务 ID")
	private Long messageId;

	@JsonStringFormat
	@Schema(name = "userId", title = "用户 ID")
	private Long userId;

	@Schema(name = "username", title = "用户名")
	private String username;

	@Schema(name = "isRead", title = "是否已读")
	private Boolean isRead;

	@Schema(name = "readTime", title = "已读时间")
	private Instant readTime;

	@Schema(name = "isDeleted", title = "用户侧是否软删除")
	private Boolean isDeleted;

}
