package com.auth.service.system.message.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息模板测试发送参数
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateTestSendForm", title = "消息模板测试发送")
@Getter
@Setter
public class MessageTemplateTestSendForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "id", title = "模板ID")
	@NotNull(message = "模板ID不能为空")
	private Long id;

	@Schema(name = "channel", title = "消息渠道")
	@NotBlank(message = "消息渠道不能为空")
	private String channel;

	@Schema(name = "target", title = "接收目标")
	@NotBlank(message = "接收目标不能为空")
	private String target;

}
