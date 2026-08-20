package com.auth.common.ip.normalize;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试{@link DefaultIpNormalizer}
 *
 * @author Bunny
 */
class DefaultIpNormalizerTest {

	private final IpNormalizer normalizer = new DefaultIpNormalizer();

	@Test
	@DisplayName("规范化应删除IPv6中的端口和括号")
	void normalize_ipv6WithBracketsAndPort() {
		// Arrange
		String raw = "[2001:db8::1]:443";

		// Act
		var normalized = normalizer.normalize(raw);

		// Assert
		assertEquals("2001:db8::1", normalized.orElse(""));
	}

	@Test
	@DisplayName("规范化应删除IPv6中的zone id")
	void normalize_ipv6WithZoneId() {
		// Arrange
		String raw = "fe80::1%eth0";

		// Act
		var normalized = normalizer.normalize(raw);

		// Assert
		assertEquals("fe80::1", normalized.orElse(""));
	}

	@Test
	@DisplayName("规范化应返回空Optional")
	void normalize_blank() {
		// Arrange
		String raw = "   ";

		// Act
		var normalized = normalizer.normalize(raw);

		// Assert
		assertTrue(normalized.isEmpty());
	}

}
