package com.auth.common.ip.inner;

import java.util.List;

/**
 * 复合内部IP检查器
 *
 * @author Bunny
 */
public record CompositeInnerIpChecker(List<InnerIpChecker> checkers) implements InnerIpChecker {

	/**
	 * 检查IP是否为内部IP
	 * @param ip IP地址
	 * @return 是否为内部IP
	 */
	@Override
	public boolean isInner(String ip) {
		// 遍历所有内部IP检查器
		for (InnerIpChecker checker : checkers) {
			if (checker.isInner(ip)) {
				// 如果任何一个检查器认为IP是内部IP，则返回true
				return true;
			}
		}

		// 如果所有检查器都认为IP不是内部IP，则返回false
		return false;
	}

}
