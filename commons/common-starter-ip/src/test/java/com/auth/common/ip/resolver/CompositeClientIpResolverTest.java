package com.auth.common.ip.resolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试{@link CompositeClientIpResolver}
 *
 * @author Bunny
 */
class CompositeClientIpResolverTest {

	@Test
	@DisplayName("X-Forwarded-For应选择第一个非未知客户端IP")
	void xForwardedFor_firstValid() {
		// Arrange
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("X-Forwarded-For", "unknown, 1.2.3.4, 5.6.7.8");

		ClientIpResolver resolver = new CompositeClientIpResolver(
				List.of(new XForwardedForResolver(), new RemoteAddrResolver()));

		// Act
		var ip = resolver.resolve(request);

		// Assert
		assertTrue(ip.isPresent());
		assertEquals("1.2.3.4", ip.get());
	}

	@Test
	@DisplayName("当headers为空时，应回退到远程地址")
	void fallback_remoteAddr() {
		// Arrange
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("10.0.0.1");

		ClientIpResolver resolver = new CompositeClientIpResolver(
				List.of(new XForwardedForResolver(), new XRealIpResolver(), new RemoteAddrResolver()));

		// Act
		var ip = resolver.resolve(request);

		// Assert
		assertEquals("10.0.0.1", ip.orElse(""));
	}

}
