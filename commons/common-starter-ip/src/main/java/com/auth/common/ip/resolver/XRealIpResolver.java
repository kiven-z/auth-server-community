package com.auth.common.ip.resolver;

/**
 * X-Real-IP头解析器
 *
 * @author Bunny
 */
public class XRealIpResolver extends HeaderClientIpResolver {

	public XRealIpResolver() {
		super("X-Real-IP", false);
	}

}
