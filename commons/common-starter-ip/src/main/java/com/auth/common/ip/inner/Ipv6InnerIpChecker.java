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

	private static Optional<InetAddress> parseInetAddress(String ip) {
		try {
			return Optional.of(InetAddress.getByName(ip));
		}
		catch (UnknownHostException e) {
			return Optional.empty();
		}
	}

	private static boolean isUniqueLocalAddress(Inet6Address address) {
		byte[] bytes = address.getAddress();
		boolean isNot16 = bytes.length != 16;
		if (isNot16) {
			return false;
		}

		BigInteger value = new BigInteger(1, bytes);
		BigInteger start = new BigInteger("fc000000000000000000000000000000", 16);
		BigInteger endExclusive = new BigInteger("fe000000000000000000000000000000", 16);

		return value.compareTo(start) >= 0 && value.compareTo(endExclusive) < 0;
	}

	@Override
	public boolean isInner(String ip) {
		if (CharSequenceUtil.isBlank(ip)) {
			return false;
		}

		String trimmed = CharSequenceUtil.trim(ip);
		if (CharSequenceUtil.startWithIgnoreCase(trimmed, IPV4_MAPPED_PREFIX)) {
			String ipv4 = trimmed.substring(IPV4_MAPPED_PREFIX.length());
			return ipv4Checker.isInner(ipv4);
		}

		Optional<InetAddress> address = parseInetAddress(trimmed);
		if (address.isEmpty() || !(address.get() instanceof Inet6Address inet6Address)) {
			return false;
		}

		if (inet6Address.isLoopbackAddress() || inet6Address.isLinkLocalAddress()) {
			return true;
		}

		return isUniqueLocalAddress(inet6Address);
	}

}
