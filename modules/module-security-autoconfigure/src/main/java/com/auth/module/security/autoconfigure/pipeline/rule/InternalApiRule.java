package com.auth.module.security.autoconfigure.pipeline.rule;

import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementRule;
import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.Optional;

/**
 * 内部注解规则
 *
 * @author Bunny
 */
public final class InternalApiRule implements SecurityRequirementRule {

	/**
	 * 匹配方法或类型上的内部注解
	 * @param handlerMethod 处理器方法
	 * @return 安全需求
	 */
	@Override
	public Optional<SecurityRequirement> match(HandlerMethod handlerMethod) {
		boolean onMethod = AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), InternalApi.class);
		boolean onType = AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), InternalApi.class);
		return (onMethod || onType) ? Optional.of(SecurityRequirement.INTERNAL) : Optional.empty();
	}

}
