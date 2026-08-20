package com.auth.common.ip.resolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

/**
 * 复合客户端IP解析器
 *
 * @author Bunny
 */
public record CompositeClientIpResolver(List<ClientIpResolver> resolvers) implements ClientIpResolver {

	public CompositeClientIpResolver(List<ClientIpResolver> resolvers) {
		this.resolvers = List.copyOf(resolvers);
	}

	/**
	 * 解析HTTP请求的原始客户端IP字符串
	 * @param request HTTP请求
	 * @return 原始客户端IP字符串
	 */
	@Override
	public Optional<String> resolve(HttpServletRequest request) {
		// 遍历解析器
		for (ClientIpResolver resolver : resolvers) {
			// 解析客户端IP字符串
			Optional<String> ip = resolver.resolve(request);

			// 如果解析器返回了客户端IP字符串，则返回
			if (ip.isPresent()) {
				return ip;
			}
		}

		// 如果所有解析器都返回了空客户端IP字符串，则返回空
		return Optional.empty();
	}

}
