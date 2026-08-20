package com.auth.module.security.autoconfigure.web;

import com.auth.module.security.autoconfigure.web.error.SecurityErrorResponseSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import static com.auth.module.security.contract.exception.SecurityResultCodeEnum.NOT_AUTHENTICATED;

/**
 * 写入 401/503 响应的认证失败
 *
 * @author Bunny
 */
public class ResourceServerAuthenticationEntryPoint implements AuthenticationEntryPoint {

	/**
	 * 处理认证失败
	 * @param request 请求
	 * @param response 响应
	 * @param authException 认证异常
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) {
		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
		SecurityErrorResponseSupport.write(request, response, NOT_AUTHENTICATED.getError());
	}

}
