package com.auth.service.system.message.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 站内信发送任务收件人/互动用户分页行
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskRecipientPageRowPO", title = "站内信发送任务收件人分页行")
@Getter
@Setter
@ToString
public class InAppSendTaskRecipientPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "站内信任务 ID")
	private Long messageId;

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "是否已读")
	private Boolean isRead;

	@Schema(title = "已读时间")
	private Instant readTime;

	@Schema(title = "用户侧是否软删除")
	private Boolean isDeleted;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
