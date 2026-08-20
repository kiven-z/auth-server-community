package com.auth.service.auth.support.login.strategy;

import com.auth.service.auth.model.value.login.LoginResult;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录认证策略：按命令类型执行凭证校验并返回登录结果。
 *
 * @author Bunny
 */
public interface LoginStrategy<T extends BaseLoginCommand> {

	/**
	 * 当前策略支持的命令类型（作为注册表 key）。
	 * @return 命令类型 Class
	 */
	Class<T> supports();

	/**
	 * 执行登录认证。
	 * @param command 登录命令
	 * @param request 当前 HTTP 请求（用于审计 IP/UA）；允许为 null
	 * @return 登录结果（响应、画像、日志类型）
	 */
	LoginResult authenticate(T command, HttpServletRequest request);

}
