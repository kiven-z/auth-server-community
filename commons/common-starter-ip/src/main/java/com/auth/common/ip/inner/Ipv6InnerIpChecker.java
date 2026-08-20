package com.auth.common.ip.inner;

import cn.hutool.core.text.CharSequenceUtil;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * IPv6内部/私有IP检查器 Rules:
 * <ul>
 * <li>Loopback: ::1</li>
 * <li>Link-local: fe80::/10</li>
 * <li>ULA: fc00::/7</li>
 * <li>IPv4-mapped IPv6: ::ffff:x.x.x.x is delegated to IPv4 checker</li>
 * </ul>
 *
 * @author Bunny
 */
public record Ipv6InnerIpChecker(InnerIpChecker ipv4Checker) implements InnerIpChecker {

	private static final String IPV4_MAPPED_PREFIX = "::ffff:";

	/**
	 * 解析IP地址
	 * @param ip IP地址
	 * @return IP地址
	 */
	private static Optional<InetAddress> parseInetAddress(String ip) {
		// 解析IP地址
		try {
			// 返回IP地址
			return Optional.of(InetAddress.getByName(ip));
		}
		catch (UnknownHostException e) {
			return Optional.empty();
		}
	}

	/**
	 * 检查IP地址是否为唯一本地地址
	 * @param address IP地址
	 * @return 是否为唯一本地地址
	 */
	private static boolean isUniqueLocalAddress(Inet6Address address) {
		// 获取IP地址的字节数组
		byte[] bytes = address.getAddress();
		// 如果IP地址的字节数组为空或长度不为16，则返回false
		boolean isNot16 = bytes.length != 16;
		if (isNot16) {
			return false;
		}

		// 创建IP地址的BigInteger值
		BigInteger value = new BigInteger(1, bytes);
		// 创建唯一本地地址的起始值
		BigInteger start = new BigInteger("fc000000000000000000000000000000", 16);
		// 创建唯一本地地址的结束值
		BigInteger endExclusive = new BigInteger("fe000000000000000000000000000000", 16);

		// 如果IP地址的值大于等于起始值且小于结束值，则返回true
		return value.compareTo(start) >= 0 && value.compareTo(endExclusive) < 0;
	}

	/**
	 * 检查IP是否为内部/私有IP
	 * @param ip IP地址
	 * @return 是否为内部/私有IP
	 */
	@Override
	public boolean isInner(String ip) {
		// 如果IP为空，则返回false
		if (CharSequenceUtil.isBlank(ip)) {
			return false;
		}

		// 修剪IP地址
		String trimmed = CharSequenceUtil.trim(ip);
		if (CharSequenceUtil.startWithIgnoreCase(trimmed, IPV4_MAPPED_PREFIX)) {
			// 如果IP地址以IPv4映射前缀开头，则检查IPv4地址
			String ipv4 = trimmed.substring(IPV4_MAPPED_PREFIX.length());
			return ipv4Checker.isInner(ipv4);
		}

		// 解析IP地址
		Optional<InetAddress> address = parseInetAddress(trimmed);
		// 如果IP地址不是IPv6地址，则返回false
		if (address.isEmpty() || !(address.get() instanceof Inet6Address inet6Address)) {
			return false;
		}

		// 如果IP地址是环回地址或链路本地地址，则返回true
		if (inet6Address.isLoopbackAddress() || inet6Address.isLinkLocalAddress()) {
			return true;
		}

		// 检查IP地址是否为唯一本地地址
		return isUniqueLocalAddress(inet6Address);
	}

}
