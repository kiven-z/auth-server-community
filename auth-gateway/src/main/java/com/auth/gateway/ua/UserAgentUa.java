package com.auth.gateway.ua;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.web.model.entity.UserAgent;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 抽象用户代理过滤器：取 UA → 规范化（跨解析器一致）→ 写请求头 → 放行
 *
 * @author Bunny
 */
@Slf4j
@Component
public class UserAgentUa implements GlobalFilter, Ordered {

	/**
	 * User-Agent 请求头
	 */
	private static final String USER_AGENT_HEADER = "User-Agent";

	private final UserAgentAnalyzer userAgentAnalyzer;

	public UserAgentUa(UserAgentAnalyzer userAgentAnalyzer) {
		this.userAgentAnalyzer = userAgentAnalyzer;
	}

	/**
	 * 解析User-Agent字符串
	 * @param request 请求
	 * @param userAgentString User-Agent字符串
	 * @return 解析后的请求
	 */
	private ServerHttpRequest parserKind(ServerHttpRequest request, String userAgentString) {
		// 使用YAUAA解析User-Agent字符串
		nl.basjes.parse.useragent.UserAgent parsedAgent = userAgentAnalyzer.parse(userAgentString);

		// 提取所需字段
		String deviceType = parsedAgent.getValue(YauaaUserAgentFields.DEVICE_CLASS);
		String deviceName = parsedAgent.getValue(YauaaUserAgentFields.DEVICE_NAME);
		String browser = parsedAgent.getValue(YauaaUserAgentFields.AGENT_NAME_VERSION);
		String os = parsedAgent.getValue(YauaaUserAgentFields.OPERATING_SYSTEM_NAME_VERSION);

		// 记录解析结果
		log.debug("UserAgent parse result - Device type: {}, Device name: {}, Browser: {}, Operating system: {}",
				deviceType, deviceName, browser, os);

		// 创建并返回UserAgent对象
		return request.mutate()
			.header(UserAgent.DEVICE_TYPE, CharSequenceUtil.blankToDefault(deviceType, ""))
			.header(UserAgent.DEVICE_NAME, CharSequenceUtil.blankToDefault(deviceName, ""))
			.header(UserAgent.AGENT_NAME_VERSION, CharSequenceUtil.blankToDefault(browser, ""))
			.header(UserAgent.OPERATING_SYSTEM_NAME, CharSequenceUtil.blankToDefault(os, ""))
			.build();
	}

	/**
	 * 过滤器方法：取 UA → 规范化（跨解析器一致）→ 写请求头 → 放行
	 * @param exchange 交换机
	 * @param chain 过滤器链
	 * @return 过滤后的交换机
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 获取请求头中的 User-Agent
		ServerHttpRequest request = exchange.getRequest();
		String userAgentString = request.getHeaders().getFirst(USER_AGENT_HEADER);

		try {
			// 如果 User-Agent 为空，则放行
			if (CharSequenceUtil.isBlank(userAgentString)) {
				return chain.filter(exchange);
			}

			// 规范化 User-Agent
			ServerHttpRequest modifiedRequest = parserKind(request, userAgentString);

			// 放行
			return chain.filter(exchange.mutate().request(modifiedRequest).build());
		}
		catch (Exception e) {
			log.warn("Failed to parse UserAgent: {}", userAgentString, e);
			return chain.filter(exchange);
		}
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE + 2;
	}

}
