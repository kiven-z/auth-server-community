package com.auth.gateway.ua;

import com.auth.common.web.model.entity.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserAgentUa 单元测试类
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class UserAgentUaTest {

	private UserAgentUa userAgentUa;

	@Mock
	private GatewayFilterChain filterChain;

	@BeforeEach
	void setUp() {
		// 创建真实的UserAgentAnalyzer，因为它是final类，Mockito无法mock
		UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
			.withCache(10000)
			.withField(YauaaUserAgentFields.DEVICE_CLASS)
			.withField(YauaaUserAgentFields.DEVICE_NAME)
			.withField(YauaaUserAgentFields.AGENT_NAME_VERSION)
			.withField(YauaaUserAgentFields.OPERATING_SYSTEM_NAME_VERSION)
			.build();
		userAgentUa = new UserAgentUa(userAgentAnalyzer);
	}

	@Test
	@DisplayName("测试构造函数正确注入依赖")
	void testConstructor_Injection() {
		// 验证构造函数正确注入依赖
		assertThat(userAgentUa).isNotNull();
	}

	@Test
	@DisplayName("测试使用有效的User-Agent字符串成功")
	void testFilter_WithValidUserAgent_Success() {
		// 准备测试数据 - 使用真实的User-Agent字符串
		String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/test")
			.header("User-Agent", userAgentString)
			.build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);

		// 模拟 filterChain 行为
		when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// 执行过滤器
		Mono<Void> result = userAgentUa.filter(exchange, filterChain);

		// 验证
		StepVerifier.create(result).verifyComplete();

		// 验证filterChain被调用，并捕获参数检查请求头
		ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
		verify(filterChain).filter(exchangeCaptor.capture());

		ServerWebExchange capturedExchange = exchangeCaptor.getValue();
		// 验证请求头被添加（具体值取决于YAUAA解析结果）
		assertThat(capturedExchange.getRequest().getHeaders().getFirst(UserAgent.DEVICE_TYPE)).isNotNull();
		assertThat(capturedExchange.getRequest().getHeaders().getFirst(UserAgent.DEVICE_NAME)).isNotNull();
		assertThat(capturedExchange.getRequest().getHeaders().getFirst(UserAgent.AGENT_NAME_VERSION)).isNotNull();
		assertThat(capturedExchange.getRequest().getHeaders().getFirst(UserAgent.OPERATING_SYSTEM_NAME)).isNotNull();
	}

	@Test
	@DisplayName("测试使用空的User-Agent字符串跳过")
	void testFilter_WithEmptyUserAgent_Skip() {
		// 准备测试数据 - 空 User-Agent
		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/test")
			.header("User-Agent", "")
			.build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);

		when(filterChain.filter(exchange)).thenReturn(Mono.empty());

		// 执行过滤器
		Mono<Void> result = userAgentUa.filter(exchange, filterChain);

		// 验证
		StepVerifier.create(result).verifyComplete();
		verify(filterChain).filter(exchange);
		// 验证请求头没有被添加（因为跳过了解析）
		assertThat(exchange.getRequest().getHeaders().getFirst(UserAgent.DEVICE_TYPE)).isNull();
	}

	@Test
	@DisplayName("测试使用空的User-Agent字符串跳过")
	void testFilter_WithNullUserAgent_Skip() {
		// 准备测试数据 - 没有 User-Agent 头
		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/test").build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);

		when(filterChain.filter(exchange)).thenReturn(Mono.empty());

		// 执行过滤器
		Mono<Void> result = userAgentUa.filter(exchange, filterChain);

		// 验证
		StepVerifier.create(result).verifyComplete();
		verify(filterChain).filter(exchange);
		// 验证请求头没有被添加（因为跳过了解析）
		assertThat(exchange.getRequest().getHeaders().getFirst(UserAgent.DEVICE_TYPE)).isNull();
	}

	@Test
	@DisplayName("测试使用无效的User-Agent字符串解析异常并继续")
	void testFilter_WithParseException_LogAndContinue() {
		// 准备测试数据 - 使用无效的User-Agent字符串
		// YAUAA可能会解析任何字符串，但我们可以测试异常处理逻辑
		// 这里我们使用一个可能引起问题的字符串
		String userAgentString = "Invalid/UserAgent String With Special \0 Null Character";
		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/test")
			.header("User-Agent", userAgentString)
			.build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);

		when(filterChain.filter(exchange)).thenReturn(Mono.empty());

		// 执行过滤器 - 即使解析有问题也应该继续
		Mono<Void> result = userAgentUa.filter(exchange, filterChain);

		// 验证
		StepVerifier.create(result).verifyComplete();
		verify(filterChain).filter(exchange);
	}

	@Test
	@DisplayName("测试解析所有字段成功")
	void testParserKind_AllFieldsPresent() throws Exception {
		// 使用反射测试私有方法
		Method parserKindMethod = UserAgentUa.class.getDeclaredMethod("parserKind",
				org.springframework.http.server.reactive.ServerHttpRequest.class, String.class);
		parserKindMethod.setAccessible(true);

		// 准备测试数据
		String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/test")
			.header("User-Agent", userAgentString)
			.build();

		// 执行私有方法
		org.springframework.http.server.reactive.ServerHttpRequest modifiedRequest = (org.springframework.http.server.reactive.ServerHttpRequest) parserKindMethod
			.invoke(userAgentUa, request, userAgentString);

		// 验证请求头被正确添加
		assertThat(modifiedRequest.getHeaders().getFirst(UserAgent.DEVICE_TYPE)).isNotNull();
		assertThat(modifiedRequest.getHeaders().getFirst(UserAgent.DEVICE_NAME)).isNotNull();
		assertThat(modifiedRequest.getHeaders().getFirst(UserAgent.AGENT_NAME_VERSION)).isNotNull();
		assertThat(modifiedRequest.getHeaders().getFirst(UserAgent.OPERATING_SYSTEM_NAME)).isNotNull();
	}

	@Test
	@DisplayName("测试获取过滤器顺序正确")
	void testGetOrder_ReturnsCorrectValue() {
		// 验证过滤器顺序
		int order = userAgentUa.getOrder();
		assertThat(order).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 2);
	}

	@Test
	@DisplayName("测试使用特殊字符的User-Agent字符串成功")
	void testFilter_WithSpecialCharactersInUserAgent() {
		// 测试特殊字符的User-Agent
		String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 \"Test\" 'Test'";
		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/test")
			.header("User-Agent", userAgentString)
			.build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);

		when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// 执行过滤器
		Mono<Void> result = userAgentUa.filter(exchange, filterChain);

		// 验证
		StepVerifier.create(result).verifyComplete();
		verify(filterChain).filter(any(ServerWebExchange.class));
	}

	@Test
	@DisplayName("测试使用超长的User-Agent字符串成功")
	void testFilter_WithVeryLongUserAgent() {
		// 测试超长User-Agent
		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/test")
			.header("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" + " Extra/Info".repeat(100))
			.build();
		ServerWebExchange exchange = MockServerWebExchange.from(request);

		when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// 执行过滤器
		Mono<Void> result = userAgentUa.filter(exchange, filterChain);

		// 验证
		StepVerifier.create(result).verifyComplete();
		verify(filterChain).filter(any(ServerWebExchange.class));
	}

}