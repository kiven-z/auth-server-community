package com.auth.service.system.message.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 用户侧站内信收件箱分页行
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxPageRowPO", title = "站内信收件箱分页行")
@Getter
@Setter
@ToString
public class InAppInboxPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "消息 ID")
	private Long id;

	@Schema(title = "场景编码")
	private String sceneCode;

	@Schema(title = "标题")
	private String title;

	@Schema(title = "正文类型")
	private String contentType;

	@Schema(title = "业务小类 ID")
	private Long categoryId;

	@Schema(title = "业务小类名称")
	private String categoryName;

	@Schema(title = "跳转链接")
	private String linkUrl;

	@Schema(title = "发起人用户 ID")
	private Long senderUserId;

	@Schema(title = "是否已读")
	private Boolean isRead;

	@Schema(title = "已读时间")
	private Instant readTime;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
