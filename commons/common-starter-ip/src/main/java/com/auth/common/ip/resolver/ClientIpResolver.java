package com.auth.common.ip.resolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * 解析HTTP请求的原始客户端IP字符串
 *
 * @author Bunny
 */
public interface ClientIpResolver {

	/**
	 * 解析HTTP请求的原始客户端IP字符串
	 * @param request HTTP请求
	 * @return 原始客户端IP字符串
	 */
	Optional<String> resolve(HttpServletRequest request);

}
