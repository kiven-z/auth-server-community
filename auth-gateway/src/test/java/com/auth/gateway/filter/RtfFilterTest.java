package com.auth.gateway.filter;

import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.exception.GatewayResultCodeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RtfFilterTest {

	private RtfFilter rtfFilter;

	@Mock
	private GatewayFilterChain filterChain;

	@BeforeEach
	void setUp() {
		rtfFilter = new RtfFilter();
	}

	@Test
	@DisplayName("测试过滤器成功")
	void testFilterSuccess() {
		// 1. 准备测试数据
		URI testUri = URI.create("http://localhost:8080/api/test");
		MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, testUri).build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		// 2. 模拟 filterChain 的行为
		when(filterChain.filter(exchange)).thenReturn(Mono.empty());

		// 3. 执行过滤器
		Mono<Void> result = rtfFilter.filter(exchange, filterChain);

		// 4. 验证
		StepVerifier.create(result).verifyComplete();

		// 验证 filterChain 被调用
		verify(filterChain, times(1)).filter(exchange);
	}

	@Test
	@DisplayName("测试过滤器错误")
	void testFilterError() {
		// 1. 准备测试数据
		URI testUri = URI.create("http://localhost:8080/api/test");
		MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, testUri).build();

		ServerWebExchange exchange = MockServerWebExchange.from(request);

		// 2. 模拟 filterChain 抛出异常
		RuntimeException mockException = new RuntimeException("Test error");
		when(filterChain.filter(exchange)).thenReturn(Mono.error(mockException));

		// 3. 执行过滤器并验证异常
		Mono<Void> result = rtfFilter.filter(exchange, filterChain);

		StepVerifier.create(result)
			.expectErrorMatches(throwable -> throwable instanceof GatewayBusinessException gbe
					&& gbe.getResultCode() == GatewayResultCodeEnum.INTERNAL_ERROR)
			.verify();

		verify(filterChain, times(1)).filter(exchange);
	}

}
