package com.auth.service.system.message.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 消息模板分页行
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplatePageRowPO", title = "消息模板分页行")
@Getter
@Setter
@ToString
public class MessageTemplatePageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "消息渠道")
	private String channel;

	@Schema(title = "模板编码")
	private String templateCode;

	@Schema(title = "模板名称")
	private String templateName;

	@Schema(title = "主题/标题")
	private String subject;

	@Schema(title = "IM/站内信正文类型")
	private String imMessageType;

	@Schema(title = "发送优先级")
	private Integer priority;

	@Schema(title = "启用状态（true=启用模板，false=停用）")
	private Boolean status;

	@Schema(title = "模板描述")
	private String description;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
