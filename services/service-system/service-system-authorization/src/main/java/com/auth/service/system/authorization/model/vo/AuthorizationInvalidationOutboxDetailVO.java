package com.auth.service.system.authorization.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 授权失效 Outbox 详情
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationOutboxDetailVO", title = "授权失效 Outbox 详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class AuthorizationInvalidationOutboxDetailVO extends AuthorizationInvalidationOutboxPageVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "失效业务键 JSON")
	private String payload;

	@Schema(title = "备注")
	private String remark;

}
