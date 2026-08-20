package com.auth.service.auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 用户名密码登录请求（HTTP 入站 DTO）
 *
 * @author Bunny
 */
@Getter
@Setter
public class UsernamePasswordLoginRequest extends AbstractLoginRequest {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "username", title = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "用户名不能为空")
	private String username;

	@Schema(name = "password", title = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "密码不能为空")
	private String password;

}
