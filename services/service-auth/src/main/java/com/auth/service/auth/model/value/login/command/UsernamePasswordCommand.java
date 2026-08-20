package com.auth.service.auth.model.value.login.command;

import com.auth.service.auth.model.enums.AuthLoginLogType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 用户名密码登录命令
 *
 * @author Bunny
 */
@Getter
@Setter
public class UsernamePasswordCommand extends BaseLoginCommand {

	@Serial
	private static final long serialVersionUID = 1L;

	private String username;

	private String password;

	@Override
	public AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	@Override
	public String principalForAudit() {
		return username;
	}

}
