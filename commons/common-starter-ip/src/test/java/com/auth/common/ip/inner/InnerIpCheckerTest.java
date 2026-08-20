package com.auth.common.ip.inner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试IPv4/IPv6内部IP检查
 *
 * @author Bunny
 */
class InnerIpCheckerTest {

	private final InnerIpChecker checker = new CompositeInnerIpChecker(
			List.of(new Ipv4InnerIpChecker(), new Ipv6InnerIpChecker(new Ipv4InnerIpChecker())));

	@DisplayName("检查各种IP地址是否为内部网络")
	@ParameterizedTest(name = "IP ''{0}'' should be inner={1}")
	@CsvSource({ "192.168.1.1, true", "::1, true", "fd00::1, true", "::ffff:192.168.1.1, true", "8.8.8.8, false" })
	void isInner_variousIps(String ip, boolean expected) {
		// Act & Assert
		assertEquals(expected, checker.isInner(ip));
	}

}
