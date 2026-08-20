package com.auth.common.ip.inner;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.NetUtil;

/**
 * IPv4内部/私有IP检查器
 *
 * @author Bunny
 */
public class Ipv4InnerIpChecker implements InnerIpChecker {

	/**
	 * 检查IP是否为内部/私有IP
	 * @param ip IP地址
	 * @return 是否为内部/私有IP
	 */
	@Override
	public boolean isInner(String ip) {
		// 如果IP不是IPv4地址，则返回false
		if (!Validator.isIpv4(ip)) {
			return false;
		}

		// 如果IP是内部/私有IP，则返回true
		return NetUtil.isInnerIP(ip);
	}

}
