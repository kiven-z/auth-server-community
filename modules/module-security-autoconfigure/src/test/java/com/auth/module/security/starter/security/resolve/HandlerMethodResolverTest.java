package com.auth.module.security.starter.security.resolve;

import com.auth.module.security.autoconfigure.pipeline.resolver.HandlerMethodResolver;
import com.auth.module.security.starter.fixture.ProbeMvcFixtureController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WebMvcTest(controllers = ProbeMvcFixtureController.class)
class HandlerMethodResolverTest {

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Test
	@DisplayName("测试没有预缓存请求路径时应找到处理方法")
	void resolve_withoutPrecachedRequestPath_findsHandlerMethod() {
		HandlerMethodResolver resolver = new HandlerMethodResolver(requestMappingHandlerMapping);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/probe/public");
		request.setRequestURI("/probe/public");

		Optional<HandlerMethod> hm = resolver.resolve(request);
		assertTrue(hm.isPresent());
		assertFalse(ServletRequestPathUtils.hasParsedRequestPath(request),
				"When Resolver parseAndCache'd the path, it must clear in finally");
	}

	@Test
	@DisplayName("测试未知路径时应返回空并清除缓存")
	void resolve_unknownPath_returnsEmptyAndClearsCache() {
		HandlerMethodResolver resolver = new HandlerMethodResolver(requestMappingHandlerMapping);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/probe/does-not-exist");
		request.setRequestURI("/probe/does-not-exist");

		assertTrue(resolver.resolve(request).isEmpty());
		assertFalse(ServletRequestPathUtils.hasParsedRequestPath(request));
	}

}
