package com.auth.service.system.message.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 站内信公开消息用户状态
 *
 * @author Bunny
 */
@TableName("in_app_message_user_status")
@Schema(name = "InAppMessageUserStatusEntity", title = "站内信公开消息用户状态")
@Getter
@Setter
@Accessors(chain = true)
public class InAppMessageUserStatusEntity extends BaseEntity {

	@Schema(title = "站内信 ID")
	private Long messageId;

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "是否已读")
	private Boolean isRead;

	@Schema(title = "已读时间")
	private Instant readTime;

	@Schema(title = "用户软删除标记")
	private Boolean isDeleted;

}
