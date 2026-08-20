package com.auth.common.ip.inner;

/**
 * 检查IP地址是否为内部/私有IP
 *
 * @author Bunny
 */
public interface InnerIpChecker {

	/**
	 * 检查IP是否为内部/私有IP
	 * @param ip 规范化后的IP字符串
	 * @return 是否为内部/私有IP
	 */
	boolean isInner(String ip);

}
