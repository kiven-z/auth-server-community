package com.auth.common.ip.region;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试{@link RoutingRegionResolver}
 *
 * @author Bunny
 */
class RoutingRegionResolverTest {

	@Test
	@DisplayName("路由解析器应使用IPv4解析器解析IPv4地址")
	void route_ipv4() {
		// Arrange
		RegionResolver v4 = ip -> Optional.of("v4");
		RegionResolver v6 = ip -> Optional.of("v6");
		RegionResolver resolver = new RoutingRegionResolver(v4, v6, new NoopRegionResolver());

		// Act
		String actual = resolver.resolveRegion("1.2.3.4").orElse("");

		// Assert
		assertEquals("v4", actual);
	}

	@Test
	@DisplayName("路由解析器应使用IPv6解析器解析IPv6地址")
	void route_ipv6() {
		// Arrange
		RegionResolver v4 = ip -> Optional.of("v4");
		RegionResolver v6 = ip -> Optional.of("v6");
		RegionResolver resolver = new RoutingRegionResolver(v4, v6, new NoopRegionResolver());

		// Act
		String actual = resolver.resolveRegion("2001:db8::1").orElse("");

		// Assert
		assertEquals("v6", actual);
	}

	@Test
	@DisplayName("路由解析器应使用回退当IP版本未知时")
	void route_unknown() {
		// Arrange
		RegionResolver v4 = ip -> Optional.of("v4");
		RegionResolver v6 = ip -> Optional.of("v6");
		RegionResolver fallback = ip -> Optional.of("noop");
		RegionResolver resolver = new RoutingRegionResolver(v4, v6, fallback);

		// Act
		String actual = resolver.resolveRegion("not-an-ip").orElse("");

		// Assert
		assertEquals("noop", actual);
	}

}
