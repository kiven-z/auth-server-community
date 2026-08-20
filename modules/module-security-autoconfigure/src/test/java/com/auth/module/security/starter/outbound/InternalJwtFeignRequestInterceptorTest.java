package com.auth.module.security.starter.outbound;

import com.auth.module.security.autoconfigure.outbound.InternalJwtFeignRequestInterceptor;
import com.auth.module.security.autoconfigure.outbound.OutboundInternalJwtIssuer;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InternalJwtFeignRequestInterceptorTest {

	private static final String SERVICE_ID = "auth-server";

	@AfterEach
	void tearDown() {
		// 清理 SecurityContext，避免影响其他测试
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("测试应签发服务令牌并移除外部授权头")
	void apply_shouldSignServiceTokenAndStripAuthorization() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(
				new OutboundInternalJwtIssuer(provider, SERVICE_ID));

		RequestTemplate template = new RequestTemplate();
		template.header(HttpHeaders.AUTHORIZATION, "Bearer external.token");

		interceptor.apply(template);

		Collection<String> authValues = template.headers().get(HttpHeaders.AUTHORIZATION);
		assertTrue(authValues == null || authValues.isEmpty(), "Authorization should be stripped on outbound");
		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("svc.internal.jwt", internal.iterator().next());
		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
	}

	@Test
	@DisplayName("测试应覆盖已有内部令牌头，避免多值污染")
	void apply_shouldReplaceExistingInternalHeader() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(
				new OutboundInternalJwtIssuer(provider, SERVICE_ID));

		RequestTemplate template = new RequestTemplate();
		template.header(SecurityInternalTokenConstants.INTERNAL_HEADER, "stale-token");
		interceptor.apply(template);

		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("svc.internal.jwt", internal.iterator().next());
		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
	}

	@Test
	@DisplayName("测试有用户上下文时应签发用户身份令牌")
	void apply_withUserContext_shouldSignUserToken() {
		// 模拟用户登录状态
		AuthProfile mockUser = AuthProfile.builder().userId(1001L).username("testuser").permVersion(5L).build();

		Authentication auth = new TestingAuthenticationToken(mockUser, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		// 配置 mock
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildToken(eq(1001L), anyString(), eq(5L))).thenReturn("user.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(
				new OutboundInternalJwtIssuer(provider, SERVICE_ID));

		RequestTemplate template = new RequestTemplate();
		template.header(HttpHeaders.AUTHORIZATION, "Bearer external.token");
		interceptor.apply(template);

		// 验证签发了用户身份令牌，而不是服务身份令牌
		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals(1, internal.size());
		assertEquals("user.internal.jwt", internal.iterator().next());

		// 验证调用了正确的方法
		verify(provider).buildToken(eq(1001L), anyString(), eq(5L));
		verify(provider, never()).buildServiceToken(anyString(), anyString());

		// 验证 Authorization 头被移除
		Collection<String> authValues = template.headers().get(HttpHeaders.AUTHORIZATION);
		assertTrue(authValues == null || authValues.isEmpty(), "Authorization should be stripped");
	}

	@Test
	@DisplayName("测试无用户上下文时应降级为服务身份令牌")
	void apply_withoutUserContext_shouldFallbackToServiceToken() {
		// 确保 SecurityContext 为空
		SecurityContextHolder.clearContext();

		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(
				new OutboundInternalJwtIssuer(provider, SERVICE_ID));

		RequestTemplate template = new RequestTemplate();
		interceptor.apply(template);

		// 验证签发了服务身份令牌
		Collection<String> internal = template.headers().get(SecurityInternalTokenConstants.INTERNAL_HEADER);
		assertEquals("svc.internal.jwt", internal.iterator().next());

		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
		verify(provider, never()).buildToken(anyLong(), anyString(), anyLong());
	}

	@Test
	@DisplayName("测试用户ID为null时应降级为服务身份令牌")
	void apply_withNullUserId_shouldFallbackToServiceToken() {
		// 模拟用户画像但 userId 为 null
		AuthProfile mockUser = AuthProfile.builder().username("testuser").build();

		Authentication auth = new TestingAuthenticationToken(mockUser, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");

		InternalJwtFeignRequestInterceptor interceptor = new InternalJwtFeignRequestInterceptor(
				new OutboundInternalJwtIssuer(provider, SERVICE_ID));

		RequestTemplate template = new RequestTemplate();
		interceptor.apply(template);

		// 验证降级为服务身份令牌
		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
		verify(provider, never()).buildToken(anyLong(), anyString(), anyLong());
	}

}
