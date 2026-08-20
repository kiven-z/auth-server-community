package com.auth.service.system.message.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 统一消息模板
 *
 * @author Bunny
 */
@TableName("message_template")
@Schema(name = "MessageTemplateEntity", title = "统一消息模板")
@Getter
@Setter
@Accessors(chain = true)
public class MessageTemplateEntity extends BaseEntity {

	@Schema(title = "场景编码")
	private String sceneCode;

	@Schema(title = "渠道")
	private String channel;

	@Schema(title = "模板名称")
	private String templateName;

	@Schema(title = "模板描述")
	private String description;

	@Schema(title = "邮件主题")
	private String subject;

	@TableField("body_content")
	@Schema(title = "正文模板")
	private String bodyContent;

	@Schema(title = "必填变量声明 JSON")
	private String requireFields;

	@Schema(title = "厂商模板编码")
	private String providerTemplateCode;

	@Schema(title = "站内信正文类型")
	private String imMessageType;

	@TableField("channel_defaults_json")
	@Schema(title = "渠道默认选项 JSON")
	private String channelDefaultsJson;

	@Schema(title = "优先级（同 scene+channel 启用行取最大 priority）")
	private Integer priority;

	@Schema(title = "启用状态")
	private Boolean status;

}
