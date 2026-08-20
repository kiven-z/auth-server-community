package com.auth.common.ip.resolver;

/**
 * HTTP_CLIENT_IP头解析器
 *
 * @author Bunny
 */
public class HttpClientIpResolver extends HeaderClientIpResolver {

	public HttpClientIpResolver() {
		super("HTTP_CLIENT_IP", false);
	}

}
