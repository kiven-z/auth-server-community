package com.auth.common.ip.resolver;

/**
 * X-Forwarded-For头解析器
 *
 * @author Bunny
 */
public class XForwardedForResolver extends HeaderClientIpResolver {

	public XForwardedForResolver() {
		super("X-Forwarded-For", true);
	}

}
