package com.auth.module.security.starter.outbound;

import com.auth.module.security.autoconfigure.outbound.OutboundInternalJwtIssuer;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link OutboundInternalJwtIssuer} 单元测试。
 */
@DisplayName("OutboundInternalJwtIssuer 出站内部 JWT")
class OutboundInternalJwtIssuerTest {

	private static final String SERVICE_ID = "service-system";

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("issueInternalToken：无用户上下文时应签发服务身份令牌")
	void issueInternalToken_withoutUserContext_shouldSignServiceToken() {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");
		OutboundInternalJwtIssuer jwtIssuer = new OutboundInternalJwtIssuer(provider, SERVICE_ID);

		assertEquals("svc.internal.jwt", jwtIssuer.issueInternalToken());

		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
		verify(provider, never()).buildToken(anyLong(), anyString(), anyLong());
	}

	@Test
	@DisplayName("issueInternalToken：有用户上下文时应签发用户身份令牌")
	void issueInternalToken_withUserContext_shouldSignUserToken() {
		AuthProfile mockUser = AuthProfile.builder().userId(1001L).username("testuser").permVersion(5L).build();
		Authentication auth = new TestingAuthenticationToken(mockUser, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildToken(eq(1001L), anyString(), eq(5L))).thenReturn("user.internal.jwt");
		OutboundInternalJwtIssuer jwtIssuer = new OutboundInternalJwtIssuer(provider, SERVICE_ID);

		assertEquals("user.internal.jwt", jwtIssuer.issueInternalToken());

		verify(provider).buildToken(eq(1001L), anyString(), eq(5L));
		verify(provider, never()).buildServiceToken(anyString(), anyString());
	}

	@Test
	@DisplayName("issueInternalToken：用户 ID 为空时应降级为服务身份令牌")
	void issueInternalToken_withNullUserId_shouldFallbackToServiceToken() {
		AuthProfile mockUser = AuthProfile.builder().username("testuser").build();
		Authentication auth = new TestingAuthenticationToken(mockUser, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");
		OutboundInternalJwtIssuer jwtIssuer = new OutboundInternalJwtIssuer(provider, SERVICE_ID);

		assertEquals("svc.internal.jwt", jwtIssuer.issueInternalToken());

		verify(provider).buildServiceToken(eq(SERVICE_ID), anyString());
		verify(provider, never()).buildToken(anyLong(), anyString(), anyLong());
	}

}
