package com.auth.service.auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 邮箱验证码登录请求（HTTP 入站 DTO）
 *
 * @author Bunny
 */
@Getter
@Setter
public class EmailLoginRequest extends AbstractLoginRequest {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "email", title = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
	@Email(message = "邮箱格式不正确")
	@NotBlank(message = "邮箱不能为空")
	private String email;

	@Schema(name = "code", title = "验证码或密码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "验证码或密码不能为空")
	private String code;

}
