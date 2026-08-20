package com.auth.module.security.starter.security.authorize;

import com.auth.module.security.autoconfigure.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfigure.security.AnnotationOverridingAuthorizationManager;
import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationOverridingAuthorizationManagerTest {

	private static final List<String> PERMIT = List.of("/public/**", "/health");

	private static AnnotationOverridingAuthorizationManager manager() {
		SecurityConfigProperties p = new SecurityConfigProperties();
		p.setPermitPaths(AnnotationOverridingAuthorizationManagerTest.PERMIT);
		return new AnnotationOverridingAuthorizationManager(p);
	}

	private static void assertGranted(AuthorizationResult result) {
		assertNotNull(result);
		assertTrue(result.isGranted());
	}

	private static void assertDenied(AuthorizationResult result) {
		assertNotNull(result);
		assertFalse(result.isGranted());
	}

	@Test
	@DisplayName("测试公共要求即使在URI匹配安全路径时也应授予")
	void publicRequirement_grantsEvenWhenUriMatchesSecuredPaths() {
		AnnotationOverridingAuthorizationManager mgr = manager();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/example/conflict");
		request.setContextPath("");
		request.setServletPath("/api/example/conflict");
		request.setRequestURI("/api/example/conflict");
		request.setAttribute(SecurityRequestAttributes.REQUIREMENT, SecurityRequirement.PUBLIC);
		RequestAuthorizationContext ctx = new RequestAuthorizationContext(request);
		AuthorizationResult result = mgr.authorize(
				() -> new AnonymousAuthenticationToken("k", "n", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")),
				ctx);
		assertGranted(result);
	}

	@Test
	@DisplayName("测试认证要求即使在允许路径上也应拒绝匿名用户")
	void authenticatedRequirement_deniesAnonymousEvenOnPermitPath() {
		AnnotationOverridingAuthorizationManager mgr = manager();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
		request.setContextPath("");
		request.setServletPath("/health");
		request.setRequestURI("/health");
		request.setAttribute(SecurityRequestAttributes.REQUIREMENT, SecurityRequirement.AUTHENTICATED);
		RequestAuthorizationContext ctx = new RequestAuthorizationContext(request);
		AuthorizationResult result = mgr.authorize(
				() -> new AnonymousAuthenticationToken("k", "n", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")),
				ctx);
		assertDenied(result);
	}

	@Test
	@DisplayName("测试认证要求在非匿名认证时应授予")
	void authenticatedRequirement_grantsWhenNonAnonymousAuthenticated() {
		AnnotationOverridingAuthorizationManager mgr = manager();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
		request.setContextPath("");
		request.setServletPath("/health");
		request.setRequestURI("/health");
		request.setAttribute(SecurityRequestAttributes.REQUIREMENT, SecurityRequirement.AUTHENTICATED);
		RequestAuthorizationContext ctx = new RequestAuthorizationContext(request);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", "n/a",
				AuthorityUtils.createAuthorityList("ROLE_USER"));
		AuthorizationResult result = mgr.authorize(() -> token, ctx);
		assertGranted(result);
	}

	@Test
	@DisplayName("测试回退时无属性时应授予允许路径")
	void fallback_noAttribute_permitPath_grantsWithoutAuthentication() {
		AnnotationOverridingAuthorizationManager mgr = manager();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/public/x");
		request.setContextPath("");
		request.setServletPath("/public/x");
		request.setRequestURI("/public/x");
		RequestAuthorizationContext ctx = new RequestAuthorizationContext(request);
		AuthorizationResult result = mgr.authorize(() -> null, ctx);
		assertGranted(result);
	}

	@Test
	@DisplayName("测试回退时非允许路径时应拒绝未认证用户")
	void fallback_nonPermitPath_deniesWhenNotAuthenticated() {
		AnnotationOverridingAuthorizationManager mgr = manager();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/foo");
		request.setContextPath("");
		request.setServletPath("/api/foo");
		request.setRequestURI("/api/foo");
		request.setAttribute(SecurityRequestAttributes.REQUIREMENT, SecurityRequirement.FALLBACK_TO_PATH);
		RequestAuthorizationContext ctx = new RequestAuthorizationContext(request);
		AuthorizationResult result = mgr.authorize(() -> null, ctx);
		assertDenied(result);
	}

	@Test
	@DisplayName("测试回退时非允许路径时应授予认证用户")
	void fallback_nonPermitPath_grantsWhenAuthenticated() {
		AnnotationOverridingAuthorizationManager mgr = manager();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/foo");
		request.setContextPath("");
		request.setServletPath("/api/foo");
		request.setRequestURI("/api/foo");
		request.setAttribute(SecurityRequestAttributes.REQUIREMENT, SecurityRequirement.FALLBACK_TO_PATH);
		RequestAuthorizationContext ctx = new RequestAuthorizationContext(request);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", "n/a",
				AuthorityUtils.createAuthorityList("ROLE_USER"));
		AuthorizationResult result = mgr.authorize(() -> token, ctx);
		assertGranted(result);
	}

	@Test
	@DisplayName("测试回退时默认要求认证时无匹配器")
	void fallback_defaultRequiresAuthenticationWhenNoMatcher() {
		AnnotationOverridingAuthorizationManager mgr = manager();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/other/path");
		request.setContextPath("");
		request.setServletPath("/other/path");
		request.setRequestURI("/other/path");
		RequestAuthorizationContext ctx = new RequestAuthorizationContext(request);
		AuthorizationResult result = mgr.authorize(() -> null, ctx);
		assertDenied(result);
	}

	@Test
	@DisplayName("测试刷新范围时应重建匹配器")
	void refreshScope_listInstanceChanges_rebuildsMatchers() {
		SecurityConfigProperties props = new SecurityConfigProperties();
		props.setPermitPaths(List.of("/a/**"));
		AnnotationOverridingAuthorizationManager mgr = new AnnotationOverridingAuthorizationManager(props);

		MockHttpServletRequest reqA = new MockHttpServletRequest("GET", "/a/x");
		reqA.setContextPath("");
		reqA.setServletPath("/a/x");
		reqA.setRequestURI("/a/x");
		assertGranted(mgr.authorize(() -> null, new RequestAuthorizationContext(reqA)));

		props.setPermitPaths(List.of("/b/**"));

		MockHttpServletRequest reqB = new MockHttpServletRequest("GET", "/b/x");
		reqB.setContextPath("");
		reqB.setServletPath("/b/x");
		reqB.setRequestURI("/b/x");
		assertGranted(mgr.authorize(() -> null, new RequestAuthorizationContext(reqB)));

		MockHttpServletRequest reqAagain = new MockHttpServletRequest("GET", "/a/x");
		reqAagain.setContextPath("");
		reqAagain.setServletPath("/a/x");
		reqAagain.setRequestURI("/a/x");
		assertDenied(mgr.authorize(() -> null, new RequestAuthorizationContext(reqAagain)));
	}

}
