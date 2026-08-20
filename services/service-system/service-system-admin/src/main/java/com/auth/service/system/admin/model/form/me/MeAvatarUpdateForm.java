package com.auth.service.system.admin.model.form.me;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前用户更新头像表单
 *
 * @author Bunny
 */
@Schema(name = "MeAvatarUpdateForm", title = "当前用户头像更新")
@Getter
@Setter
public class MeAvatarUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "头像 URL", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 512, message = "头像地址长度不能超过512个字符")
	@NotBlank(message = "头像地址不能为空")
	private String avatar;

}
