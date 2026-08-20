package com.auth.service.auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求基类（HTTP 入站 DTO）
 *
 * @author Bunny
 */
@Getter
@Setter
public abstract class AbstractLoginRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "rememberMe", title = "是否记住登录")
	private Boolean rememberMe;

}
