package com.auth.service.system.message.model.form;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息模板批量启停（增加渠道）
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateStatusForm", title = "消息模板批量启用/禁用表单")
@Getter
@Setter
public class MessageTemplateStatusForm extends IdsEnableStatusForm {

	@Schema(title = "消息渠道")
	@NotBlank(message = "消息渠道不能为空")
	private String channel;

}
