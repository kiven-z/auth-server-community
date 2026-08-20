package com.auth.service.auth.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 授权失效幂等事件详情
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationEventDetailVO", title = "授权失效幂等事件详情")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AuthorizationInvalidationEventDetailVO extends AuthorizationInvalidationEventPageVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "备注")
	private String remark;

}
