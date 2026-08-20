package com.auth.service.system.message.model.vo.template;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息模板变量声明（与 require_fields JSON 数组中单条结构一致）
 *
 * @author Bunny
 */
@Schema(name = "MessageTemplateRequireFieldRow", title = "消息模板变量声明")
@Getter
@Setter
public class MessageTemplateRequireFieldRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "key", title = "字段名")
	@NotBlank(message = "变量名不能为空")
	private String key;

	@Schema(name = "description", title = "字段含义")
	private String description;

	@Schema(name = "exampleValue", title = "示例数据（可为字符串/数字/数组/对象）")
	@NotNull(message = "示例数据不能为空")
	private transient JsonNode exampleValue;

}
