package com.auth.service.auth.model.value.login.command;

import com.auth.service.auth.model.enums.AuthLoginLogType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 邮箱验证码登录命令
 *
 * @author Bunny
 */
@Getter
@Setter
public class EmailCommand extends BaseLoginCommand {

	@Serial
	private static final long serialVersionUID = 1L;

	private String email;

	private String code;

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	public String principalForAudit() {
		return email;
	}

}
