package com.auth.common.ip.resolver;

/**
 * Proxy-Client-IP头解析器
 *
 * @author Bunny
 */
public class ProxyClientIpResolver extends HeaderClientIpResolver {

	public ProxyClientIpResolver() {
		super("Proxy-Client-IP", false);
	}

}
