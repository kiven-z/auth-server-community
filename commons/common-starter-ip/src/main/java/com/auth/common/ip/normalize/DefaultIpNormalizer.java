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

	private static String removeZoneId(String ip) {
		int zoneIndex = ip.indexOf(ZONE_ID_SEPARATOR);
		if (zoneIndex <= 0) {
			return ip;
		}

		return ip.substring(0, zoneIndex);
	}

	private static String stripBracketedIpv6AndPort(String ip) {
		String prefix = "[";
		if (!CharSequenceUtil.startWith(ip, prefix)) {
			return ip;
		}

		int closeBracketIndex = ip.indexOf(']');
		if (closeBracketIndex < 0) {
			return ip;
		}

		return ip.substring(1, closeBracketIndex);
	}

	private static String stripIpv4Port(String ip) {
		int firstColon = ip.indexOf(':');
		if (firstColon <= 0) {
			return ip;
		}

		// 多个冒号是无括号的 IPv6，不剥端口
		int lastColon = ip.lastIndexOf(':');
		if (firstColon != lastColon) {
			return ip;
		}

		return ip.substring(0, firstColon);
	}

	@Override
	public Optional<String> normalize(String raw) {
		if (CharSequenceUtil.isBlank(raw)) {
			return Optional.empty();
		}

		String ip = CharSequenceUtil.trim(raw);
		ip = removeZoneId(ip);
		ip = stripBracketedIpv6AndPort(ip);
		ip = stripIpv4Port(ip);

		if (CharSequenceUtil.isBlank(ip)) {
			return Optional.empty();
		}

		return Optional.of(ip);
	}

}
