package com.auth.service.auth.service;

import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP 会话编排：登录/刷新/登出与 Refresh Cookie 写入
 *
 * @author Bunny
 */
public interface AuthSessionService {

	/**
	 * 完成登录：签发令牌、写入 Redis 会话、下发 Refresh Cookie
	 * @param command 登录命令（含 rememberMe）
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 登录 API 响应
	 */
	UserLoginResponse completeLogin(BaseLoginCommand command, HttpServletRequest request, HttpServletResponse response);

	/**
	 * 完成刷新：旋转令牌、按会话 rememberMe 重写 Refresh Cookie
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @return 刷新 API 响应
	 */
	RefreshTokenResponse completeRefresh(HttpServletRequest request, HttpServletResponse response);

	/**
	 * 完成登出：撤销服务端会话、写入登出审计并清除 Refresh Cookie
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 */
	void completeLogout(HttpServletRequest request, HttpServletResponse response);

}
