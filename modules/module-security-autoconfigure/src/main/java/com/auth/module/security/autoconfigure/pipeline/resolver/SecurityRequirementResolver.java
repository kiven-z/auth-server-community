package com.auth.module.security.autoconfigure.pipeline.resolver;

import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;

import java.util.List;
import java.util.Optional;

/**
 * 注解优先解析当前请求的安全要求
 *
 * @author Bunny
 */
public final class SecurityRequirementResolver {

	private final HandlerMethodResolver handlerMethodResolver;

	private final List<SecurityRequirementRule> rules;

	public SecurityRequirementResolver(HandlerMethodResolver handlerMethodResolver,
			List<SecurityRequirementRule> rules) {
		this.handlerMethodResolver = handlerMethodResolver;
		this.rules = rules;
	}

	/**
	 * 解析安全要求
	 * @param request HTTP 请求
	 * @return 安全要求；无注解或无法解析 handler 时为 {@link SecurityRequirement#FALLBACK_TO_PATH}
	 */
	public SecurityRequirement resolve(HttpServletRequest request) {
		// 解析 handler method
		Optional<HandlerMethod> handlerMethod = handlerMethodResolver.resolve(request);

		// 第一个匹配规则
		return handlerMethod.flatMap(this::firstMatchingRule).orElse(SecurityRequirement.FALLBACK_TO_PATH);
	}

	/**
	 * 第一个匹配规则
	 * @param hm HandlerMethod
	 * @return 安全要求
	 */
	private Optional<SecurityRequirement> firstMatchingRule(HandlerMethod hm) {
		return rules.stream().map(rule -> rule.match(hm)).flatMap(Optional::stream).findFirst();
	}

}
