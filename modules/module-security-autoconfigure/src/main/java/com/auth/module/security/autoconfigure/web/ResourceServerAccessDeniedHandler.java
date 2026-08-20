package com.auth.module.security.autoconfigure.web;

import com.auth.module.security.autoconfigure.web.error.SecurityErrorResponseSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import static com.auth.module.security.contract.exception.SecurityResultCodeEnum.ACCESS_DENIED;

/**
 * 写入 403 响应的授权失败
 *
 * @author Bunny
 */
public class ResourceServerAccessDeniedHandler implements AccessDeniedHandler {

	/**
	 * 处理授权失败
	 * @param request 请求
	 * @param response 响应
	 * @param accessDeniedException 访问拒绝异常
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		SecurityErrorResponseSupport.write(request, response, ACCESS_DENIED.getError());
	}

}
