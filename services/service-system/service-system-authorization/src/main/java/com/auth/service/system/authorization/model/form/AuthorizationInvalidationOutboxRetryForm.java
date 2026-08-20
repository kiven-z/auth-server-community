package com.auth.service.system.authorization.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效 Outbox 人工重试表单
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationOutboxRetryForm", title = "授权失效 Outbox 人工重试")
@Getter
@Setter
public class AuthorizationInvalidationOutboxRetryForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "重试原因")
	private String reason;

	@Schema(title = "强制解锁并重试")
	private Boolean force;

}
