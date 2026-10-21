package com.auth.common.ip.inner;

import java.util.List;

/**
 * 任一检查器命中即视为内网
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
				return true;
			}
		}

		return false;
	}

}
