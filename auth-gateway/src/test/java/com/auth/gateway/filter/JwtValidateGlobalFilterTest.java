package com.auth.gateway.filter;

import com.auth.gateway.config.GatewaySecurityProperties;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.exception.GatewayResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtValidateGlobalFilterTest {

	@Mock
	private AccessTokenProvider accessTokenProvider;

	@Mock
	private GatewayFilterChain filterChain;

	private JwtValidateGlobalFilter newFilter(boolean strictEnabled, List<String> strictPatterns) {
		GatewaySecurityProperties properties = new GatewaySecurityProperties();
		properties.setStrictEnabled(strictEnabled);
		properties.setStrictPatterns(strictPatterns);
		return new JwtValidateGlobalFilter(accessTokenProvider, properties);
	}

	@Test
	@DisplayName("测试轻校验路径在无 Authorization 时直接放行")
	void filter_shouldPass_whenLightPathAndAuthorizationMissing() {
		JwtValidateGlobalFilter filter = newFilter(true, List.of("/strict/**"));
		MockServerWebExchange exchange = MockServerWebExchange
			.from(MockServerHttpRequest.method(HttpMethod.GET, "/open/ping").build());
		when(filterChain.filter(exchange)).thenReturn(Mono.empty());

		StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

		verify(filterChain).filter(exchange);
		verifyNoInteractions(accessTokenProvider);
	}

	@Test
	@DisplayName("测试严格路径在无 Authorization 时返回 UNAUTHORIZED")
	void filter_shouldReject_whenStrictPathAndAuthorizationMissing() {
		JwtValidateGlobalFilter filter = newFilter(true, List.of("/strict/**"));
		MockServerWebExchange exchange = MockServerWebExchange
			.from(MockServerHttpRequest.method(HttpMethod.GET, "/strict/profile").build());

		StepVerifier.create(filter.filter(exchange, filterChain))
			.expectErrorMatches(throwable -> throwable instanceof GatewayBusinessException gbe
					&& gbe.getResultCode() == GatewayResultCodeEnum.UNAUTHORIZED)
			.verify();

		verifyNoInteractions(accessTokenProvider, filterChain);
	}

	@Test
	@DisplayName("测试轻校验路径在令牌过期时放行")
	void filter_shouldPass_whenLightPathAndTokenExpired() {
		JwtValidateGlobalFilter filter = newFilter(true, List.of("/strict/**"));
		MockServerWebExchange exchange = MockServerWebExchange
			.from(MockServerHttpRequest.method(HttpMethod.GET, "/open/api")
				.header(HttpHeaders.AUTHORIZATION, "Bearer expired-token")
				.build());
		when(accessTokenProvider.parseToken("Bearer expired-token"))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_EXPIRED, "Token has expired."));
		when(filterChain.filter(exchange)).thenReturn(Mono.empty());

		StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

		verify(filterChain).filter(exchange);
	}

	@Test
	@DisplayName("测试严格路径在令牌过期时拒绝请求")
	void filter_shouldReject_whenStrictPathAndTokenExpired() {
		JwtValidateGlobalFilter filter = newFilter(true, List.of("/strict/**"));
		MockServerWebExchange exchange = MockServerWebExchange
			.from(MockServerHttpRequest.method(HttpMethod.GET, "/strict/api")
				.header(HttpHeaders.AUTHORIZATION, "Bearer expired-token")
				.build());
		when(accessTokenProvider.parseToken("Bearer expired-token"))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_EXPIRED, "Token has expired."));

		StepVerifier.create(filter.filter(exchange, filterChain))
			.expectErrorMatches(throwable -> throwable instanceof GatewayBusinessException gbe
					&& gbe.getResultCode() == GatewayResultCodeEnum.UNAUTHORIZED)
			.verify();
	}

	@Test
	@DisplayName("测试轻校验路径在令牌无效时拒绝请求")
	void filter_shouldReject_whenTokenInvalid() {
		JwtValidateGlobalFilter filter = newFilter(false, List.of("/strict/**"));
		MockServerWebExchange exchange = MockServerWebExchange
			.from(MockServerHttpRequest.method(HttpMethod.GET, "/open/api")
				.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
				.build());
		when(accessTokenProvider.parseToken("Bearer invalid-token"))
			.thenThrow(new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "Token is invalid."));

		StepVerifier.create(filter.filter(exchange, filterChain))
			.expectErrorMatches(throwable -> throwable instanceof GatewayBusinessException gbe
					&& gbe.getResultCode() == GatewayResultCodeEnum.UNAUTHORIZED)
			.verify();
	}

}
