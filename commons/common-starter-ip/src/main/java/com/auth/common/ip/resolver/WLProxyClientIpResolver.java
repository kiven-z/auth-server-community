package com.auth.common.ip.resolver;

/**
 * WL-Proxy-Client-IP头解析器
 *
 * @author Bunny
 */
public class WLProxyClientIpResolver extends HeaderClientIpResolver {

	public WLProxyClientIpResolver() {
		super("WL-Proxy-Client-IP", false);
	}

}
