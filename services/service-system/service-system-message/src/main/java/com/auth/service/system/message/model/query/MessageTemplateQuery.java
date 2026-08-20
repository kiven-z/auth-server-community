package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息模板分页查询
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateQuery", title = "消息模板查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MessageTemplateQuery extends PageQueryRequest {

	@Schema(name = "channel", title = "消息渠道", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "消息渠道不能为空")
	private String channel;

	@Schema(name = "templateCode", title = "模板编码")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "subject", title = "主题/标题")
	private String subject;

	@Schema(name = "imMessageType", title = "站内信正文类型")
	private String imMessageType;

	@Schema(name = "status", title = "启用状态（true=启用模板，false=停用）")
	private Boolean status;

}
