package com.auth.service.system.admin.model.form.user;

import com.auth.common.web.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员重置用户密码表单
 *
 * @author Bunny
 */
@Schema(name = "SysUserAdminResetPasswordForm", title = "管理员重置用户密码")
@Getter
@Setter
public class SysUserAdminResetPasswordForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "新密码")
	@Size(min = 8, max = 18, message = "新密码长度应为8-18位")
	@Pattern(regexp = ValidationPatterns.TWO_OF_THREE_CHAR_CLASSES_8_18, message = "新密码格式不符合约定（8-18位，数字、字母、符号至少两种）")
	@NotBlank(message = "新密码不能为空")
	private String newPassword;

	@Schema(title = "确认密码")
	@NotBlank(message = "确认密码不能为空")
	private String confirmPassword;

}
