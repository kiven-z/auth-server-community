package com.auth.module.security.starter.outbound;

import com.auth.module.security.autoconfigure.outbound.InternalJwtClientHttpRequestInterceptor;
import com.auth.module.security.autoconfigure.outbound.OutboundInternalJwtIssuer;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link InternalJwtClientHttpRequestInterceptor} 单元测试。
 */
@DisplayName("InternalJwtClientHttpRequestInterceptor RestTemplate 出站")
class InternalJwtClientHttpRequestInterceptorTest {

	private static final String SERVICE_ID = "service-system";

	@Test
	@DisplayName("intercept：应剥离外部凭证并在执行前附加 X-Internal-JWT")
	void intercept_shouldStripCredentialsAndAttachInternalJwtBeforeExecution() throws IOException {
		InternalTokenProvider provider = mock(InternalTokenProvider.class);
		when(provider.buildServiceToken(eq(SERVICE_ID), anyString())).thenReturn("svc.internal.jwt");
		OutboundInternalJwtIssuer jwtIssuer = new OutboundInternalJwtIssuer(provider, SERVICE_ID);
		InternalJwtClientHttpRequestInterceptor interceptor = new InternalJwtClientHttpRequestInterceptor(jwtIssuer);

		HttpRequest request = mock(HttpRequest.class);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer external.token");
		headers.set(SecurityInternalTokenConstants.INTERNAL_HEADER, "stale-token");
		when(request.getHeaders()).thenReturn(headers);

		ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
		ClientHttpResponse response = mock(ClientHttpResponse.class);
		when(execution.execute(any(HttpRequest.class), any())).thenReturn(response);

		ClientHttpResponse actual = interceptor.intercept(request, new byte[0], execution);

		assertNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
		assertEquals("svc.internal.jwt", headers.getFirst(SecurityInternalTokenConstants.INTERNAL_HEADER));
		verify(execution).execute(request, new byte[0]);
		assertEquals(response, actual);
	}

}
