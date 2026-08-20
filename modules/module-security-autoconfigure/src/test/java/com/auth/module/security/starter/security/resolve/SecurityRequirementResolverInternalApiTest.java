package com.auth.module.security.starter.security.resolve;

import com.auth.module.security.autoconfigure.pipeline.resolver.HandlerMethodResolver;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementResolver;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementRule;
import com.auth.module.security.autoconfigure.pipeline.rule.AuthenticatedApiRule;
import com.auth.module.security.autoconfigure.pipeline.rule.InternalApiRule;
import com.auth.module.security.autoconfigure.pipeline.rule.PublicApiRule;
import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import com.auth.module.security.starter.fixture.InternalApiFixtureController;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(controllers = InternalApiFixtureController.class)
class SecurityRequirementResolverInternalApiTest {

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Test
	@DisplayName("SecurityRequirementResolver: @InternalApi 应解析为 INTERNAL")
	void resolve_internalApi_shouldReturnInternal() {
		HandlerMethodResolver handlerMethodResolver = new HandlerMethodResolver(requestMappingHandlerMapping);
		List<SecurityRequirementRule> rules = List.of(new PublicApiRule(), new InternalApiRule(),
				new AuthenticatedApiRule());
		SecurityRequirementResolver resolver = new SecurityRequirementResolver(handlerMethodResolver, rules);

		HttpServletRequest request = new MockHttpServletRequest("GET", "/probe-internal/x");
		((MockHttpServletRequest) request).setRequestURI("/probe-internal/x");

		assertEquals(SecurityRequirement.INTERNAL, resolver.resolve(request));
	}

}
