package com.auth.common.ip.region;

import java.util.Optional;

/**
 * 无操作区域解析器
 *
 * @author Bunny
 */
public class NoopRegionResolver implements RegionResolver {

	/**
	 * 解析IP地址的区域
	 * @param ip IP地址
	 * @return 区域
	 */
	@Override
	public Optional<String> resolveRegion(String ip) {
		return Optional.empty();
	}

}
