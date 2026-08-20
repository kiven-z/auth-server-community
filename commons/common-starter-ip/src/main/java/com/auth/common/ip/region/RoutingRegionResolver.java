package com.auth.common.ip.region;

import cn.hutool.core.lang.Validator;

import java.util.Optional;

/**
 * 根据IP版本路由区域解析
 *
 * @author Bunny
 */
public record RoutingRegionResolver(RegionResolver ipv4Resolver, RegionResolver ipv6Resolver,
		RegionResolver fallbackResolver) implements RegionResolver {

	/**
	 * 解析IP地址的区域
	 * @param ip IP地址
	 * @return 区域
	 */
	@Override
	public Optional<String> resolveRegion(String ip) {
		// 如果IP地址是IPv4地址，则解析IPv4地址的区域
		if (Validator.isIpv4(ip)) {
			return ipv4Resolver.resolveRegion(ip);
		}

		// 如果IP地址是IPv6地址，则解析IPv6地址的区域
		if (Validator.isIpv6(ip)) {
			return ipv6Resolver.resolveRegion(ip);
		}

		// 如果IP地址不是IPv4或IPv6地址，则解析默认区域
		return fallbackResolver.resolveRegion(ip);
	}

}
