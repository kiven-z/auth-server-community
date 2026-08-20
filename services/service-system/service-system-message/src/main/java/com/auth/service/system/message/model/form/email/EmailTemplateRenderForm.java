package com.auth.service.system.message.model.form.email;

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
 * 邮件模板离线渲染请求
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateRenderForm", title = "邮件模板渲染请求")
@Getter
@Setter
public class EmailTemplateRenderForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "content", title = "FreeMarker 模板内容")
	@NotBlank(message = "模板内容不能为空")
	private String content;

	@Schema(name = "requireFields", title = "变量声明列表")
	@Valid
	@NotNull(message = "变量声明列表不能为空")
	private List<MessageTemplateRequireFieldRow> requireFields;

}
