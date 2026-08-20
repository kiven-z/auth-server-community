package com.auth.service.system.message.model.form.email;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static com.auth.common.web.validation.ValidationPatterns.LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER;

/**
 * 邮件模板表单
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateForm", title = "邮件模板表单")
@Getter
@Setter
public class EmailTemplateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "id", title = "主键ID")
	@NotNull(groups = UpdateGroup.class, message = "更新时ID不能为空")
	private Long id;

	@Schema(name = "templateCode", title = "模板编码（唯一）")
	@Pattern(regexp = LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER, message = "模板编码只能是字母",
			groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "模板编码不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称（唯一）")
	@NotBlank(message = "模板名称不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String templateName;

	@Schema(name = "subject", title = "邮件主题")
	@NotBlank(message = "邮件主题不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String subject;

	@Schema(name = "description", title = "模板描述")
	private String description;

	@Schema(name = "status", title = "启用状态（true=启用模板，false=停用）")
	@NotNull(message = "状态不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Boolean status;

	@Schema(name = "priority", title = "发送优先级（1-10,数字越大优先级越高）")
	@NotNull(message = "发送优先级不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Integer priority;

	@Schema(name = "content", title = "邮件内容（支持HTML），可空串")
	private String content;

}
