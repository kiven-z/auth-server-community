package com.auth.common.ip.normalize;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.Optional;

/**
 * 默认的ip正常化器
 *
 * <ul>
 * <li>删除空格</li>
 * <li>删除IPv6 zone id后缀 (e.g. %eth0)</li>
 * <li>删除括号和端口 for bracketed IPv6 (e.g. [::1]:443)</li>
 * <li>删除IPv4端口 (e.g. 1.2.3.4:8080)</li>
 * </ul>
 *
 * @author Bunny
 */
public class DefaultIpNormalizer implements IpNormalizer {

	private static final char ZONE_ID_SEPARATOR = '%';

	/**
	 * 删除IPv6 zone id后缀
	 * @param ip 原始ip
	 * @return 删除后的ip
	 */
	private static String removeZoneId(String ip) {
		// 获取IPv6 zone id后缀的索引
		int zoneIndex = ip.indexOf(ZONE_ID_SEPARATOR);
		// 如果IPv6 zone id后缀不存在，则返回原始ip
		if (zoneIndex <= 0) {
			return ip;
		}

		// 返回删除IPv6 zone id后缀后的ip
		return ip.substring(0, zoneIndex);
	}

	/**
	 * 删除括号和端口 for bracketed IPv6
	 * @param ip 原始ip
	 * @return 删除后的ip
	 */
	private static String stripBracketedIpv6AndPort(String ip) {
		// 如果原始ip不以'['开头，则返回原始ip
		String prefix = "[";
		if (!CharSequenceUtil.startWith(ip, prefix)) {
			return ip;
		}

		// 获取']'的索引
		int closeBracketIndex = ip.indexOf(']');
		if (closeBracketIndex < 0) {
			return ip;
		}

		// 返回删除括号和端口后的ip
		return ip.substring(1, closeBracketIndex);
	}

	/**
	 * 删除IPv4端口
	 * @param ip 原始ip
	 * @return 删除后的ip
	 */
	private static String stripIpv4Port(String ip) {
		// 获取':'的索引
		int firstColon = ip.indexOf(':');
		if (firstColon <= 0) {
			return ip;
		}

		// 如果存在多个':',则可能是IPv6 literal without brackets.
		int lastColon = ip.lastIndexOf(':');
		if (firstColon != lastColon) {
			return ip;
		}

		return ip.substring(0, firstColon);
	}

	/**
	 * 正常化ip
	 * @param raw 原始ip
	 * @return 正常化后的ip
	 */
	@Override
	public Optional<String> normalize(String raw) {
		// 如果原始ip为空，则返回空
		if (CharSequenceUtil.isBlank(raw)) {
			return Optional.empty();
		}

		// 修剪原始ip
		String ip = CharSequenceUtil.trim(raw);
		// 删除IPv6 zone id后缀
		ip = removeZoneId(ip);
		// 删除括号和端口 for bracketed IPv6
		ip = stripBracketedIpv6AndPort(ip);
		// 删除IPv4端口
		ip = stripIpv4Port(ip);

		// 如果删除后的ip为空，则返回空
		if (CharSequenceUtil.isBlank(ip)) {
			return Optional.empty();
		}

		// 返回正常化后的ip
		return Optional.of(ip);
	}

}
