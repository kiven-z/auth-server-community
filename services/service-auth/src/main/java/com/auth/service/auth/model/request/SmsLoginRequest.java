package com.auth.service.auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 短信验证码登录请求（HTTP 入站 DTO）
 *
 * @author Bunny
 */
@Getter
@Setter
public class SmsLoginRequest extends AbstractLoginRequest {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "phone", title = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	@NotBlank(message = "手机号不能为空")
	private String phone;

	@Schema(name = "code", title = "短信验证码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "短信验证码不能为空")
	private String code;

}
