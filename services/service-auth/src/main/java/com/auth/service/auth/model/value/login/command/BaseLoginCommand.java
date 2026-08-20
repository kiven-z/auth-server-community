package com.auth.service.auth.model.value.login.command;

import com.auth.service.auth.model.enums.AuthLoginLogType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录命令基类：持有认证所需的凭证数据，无 HTTP 展示注解
 *
 * @author Bunny
 */
@Getter
@Setter
public abstract class BaseLoginCommand implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 是否记住登录
	 */
	private Boolean rememberMe;

	/**
	 * 当前登录方式对应的审计事件类型
	 * @return 登录日志类型
	 */
	public abstract AuthLoginLogType loginLogType();

	/**
	 * 审计主体（用户名、邮箱或手机号等），用于尚未解析到用户时的日志
	 * @return 主体字符串；无法解析时返回 null
	 */
	public abstract String principalForAudit();

}
