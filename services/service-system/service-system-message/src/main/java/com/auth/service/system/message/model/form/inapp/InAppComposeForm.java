package com.auth.service.system.message.model.form.inapp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端站内信按范围发送表单
 *
 * @author Bunny
 */
@Schema(name = "InAppComposeForm", title = "站内信按范围发送")
@Getter
@Setter
public class InAppComposeForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "接收范围类型")
	@NotBlank(message = "接收范围类型不能为空")
	private String recipientScopeType;

	@Schema(title = "范围 ID 列表")
	private List<Long> recipientScopeIds;

	@Schema(title = "部门是否包含子部门")
	private Boolean includeChildren;

	@Schema(title = "模板场景编码")
	private String templateCode;

	@Schema(title = "标题（定稿）")
	@NotBlank(message = "标题不能为空")
	private String title;

	@Schema(title = "正文（定稿）")
	@NotBlank(message = "正文不能为空")
	private String body;

	@Schema(title = "正文类型")
	@NotBlank(message = "正文类型不能为空")
	private String contentType;

	@Schema(title = "业务小类 ID")
	@NotNull(message = "业务小类不能为空")
	private Long categoryId;

	@Schema(title = "跳转链接")
	private String linkUrl;

}
