package com.auth.service.system.admin.model.form.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员更新用户头像表单
 *
 * @author Bunny
 */
@Schema(name = "SysUserAvatarUpdateForm", title = "管理员更新用户头像")
@Getter
@Setter
public class SysUserAvatarUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "用户 ID 不能为空")
	private Long userId;

	@Schema(title = "头像 URL", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 512, message = "头像地址长度不能超过512个字符")
	@NotBlank(message = "头像地址不能为空")
	private String avatar;

}
