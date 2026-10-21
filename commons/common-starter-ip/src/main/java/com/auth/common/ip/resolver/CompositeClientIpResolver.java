package com.auth.common.ip.resolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

/**
 * 按顺序取第一个非空结果
 *
 * @author Bunny
 */
public record CompositeClientIpResolver(List<ClientIpResolver> resolvers) implements ClientIpResolver {

	public CompositeClientIpResolver(List<ClientIpResolver> resolvers) {
		this.resolvers = List.copyOf(resolvers);
	}

	@Override
	public Optional<String> resolve(HttpServletRequest request) {
		for (ClientIpResolver resolver : resolvers) {
			Optional<String> ip = resolver.resolve(request);
			if (ip.isPresent()) {
				return ip;
			}
		}

		return Optional.empty();
	}

}
