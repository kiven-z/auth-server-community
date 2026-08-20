package com.auth.common.ip.resolver;

/**
 * HTTP_X_FORWARDED_FOR头解析器
 *
 * @author Bunny
 */
public class HttpXForwardedForResolver extends HeaderClientIpResolver {

	public HttpXForwardedForResolver() {
		super("HTTP_X_FORWARDED_FOR", true);
	}

}
