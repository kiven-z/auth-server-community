package com.auth.module.security.autoconfigure.pipeline.rule;

import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementRule;
import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.Optional;

/**
 * {@link PublicApi} 映射为 {@link SecurityRequirement#PUBLIC}
 *
 * @author Bunny
 */
public final class PublicApiRule implements SecurityRequirementRule {

	/**
	 * 匹配方法或类型上的 {@link PublicApi} 注解
	 * @param handlerMethod 处理器方法
	 * @return 安全需求
	 */
	@Override
	public Optional<SecurityRequirement> match(HandlerMethod handlerMethod) {
		boolean onMethod = AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), PublicApi.class);
		boolean onType = AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), PublicApi.class);
		return (onMethod || onType) ? Optional.of(SecurityRequirement.PUBLIC) : Optional.empty();
	}

}
