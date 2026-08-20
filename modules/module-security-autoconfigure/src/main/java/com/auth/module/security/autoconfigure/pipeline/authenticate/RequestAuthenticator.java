package com.auth.module.security.autoconfigure.pipeline.authenticate;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.exception.SecurityTokenException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 按请求头选择并执行认证，返回已加载的 AuthProfile
 *
 * @author Bunny
 */
public interface RequestAuthenticator {

	/**
	 * 是否支持处理当前请求
	 * @param request HTTP 请求
	 * @return 是否支持
	 */
	boolean supports(HttpServletRequest request);

	/**
	 * 执行认证并返回授权画像
	 * @param request HTTP 请求
	 * @return 授权画像
	 * @throws SecurityTokenException 认证失败
	 */
	AuthProfile authenticate(HttpServletRequest request) throws SecurityTokenException;

}
