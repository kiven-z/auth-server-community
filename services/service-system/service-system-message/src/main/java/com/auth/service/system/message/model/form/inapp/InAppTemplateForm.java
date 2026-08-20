package com.auth.service.system.message.model.form.inapp;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static com.auth.common.web.validation.ValidationPatterns.LETTER_UNDERSCORE_HYPHEN_REQUIRE_LETTER;

/**
 * 站内信模板表单
 *
 * @author Bunny
 */
@Schema(name = "InAppTemplateForm", title = "站内信模板表单")
@Getter
@Setter
public class InAppTemplateForm implements Serializable {

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

	@Schema(name = "templateName", title = "模板名称")
	@NotBlank(message = "模板名称不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String templateName;

	@Schema(name = "subject", title = "标题模板")
	@NotBlank(message = "标题不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String subject;

	@Schema(name = "contentType", title = "正文类型")
	@NotBlank(message = "正文类型不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String contentType;

	@Schema(name = "content", title = "模板正文")
	@NotBlank(message = "模板正文不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String content;

	@Schema(name = "description", title = "模板描述")
	private String description;

	@Schema(name = "status", title = "启用状态（true=启用模板，false=停用）")
	@NotNull(message = "状态不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Boolean status;

	@Schema(name = "priority", title = "发送优先级（1-10,数字越大优先级越高）")
	@NotNull(message = "发送优先级不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Integer priority;

	@Schema(title = "默认业务小类 ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "业务小类不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private Long categoryId;

	@Schema(title = "默认跳转链接")
	@Size(max = 500, message = "跳转链接长度不能超过500个字符", groups = { CreateGroup.class, UpdateGroup.class })
	private String linkUrl;

}
