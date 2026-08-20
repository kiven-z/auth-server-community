package com.auth.service.system.message.model.form;

import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 仅更新消息模板 require_fields
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateRequireFieldsForm", title = "消息模板变量列表表单")
@Getter
@Setter
public class MessageTemplateRequireFieldsForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "id", title = "模板主键")
	@NotNull(message = "模板ID不能为空")
	private Long id;

	@Schema(name = "channel", title = "消息渠道")
	@NotBlank(message = "消息渠道不能为空")
	private String channel;

	@Schema(name = "requireFields", title = "变量声明列表（允许空数组）")
	@Valid
	@NotNull(message = "变量列表不能为 null")
	private List<MessageTemplateRequireFieldRow> requireFields;

}
