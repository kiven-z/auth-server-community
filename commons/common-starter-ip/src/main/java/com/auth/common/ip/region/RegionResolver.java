package com.auth.common.ip.region;

import java.util.Optional;

/**
 * 解析IP地址的区域
 *
 * @author Bunny
 */
public interface RegionResolver {

	/**
	 * 解析IP地址的区域
	 * @param ip IP地址
	 * @return 区域
	 */
	Optional<String> resolveRegion(String ip);

}
