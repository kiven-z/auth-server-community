package com.auth.module.security.autoconfigure.pipeline.resolver;

import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import org.springframework.web.method.HandlerMethod;

import java.util.Optional;

/**
 * 将处理器上的安全注解映射为 {@link SecurityRequirement}
 *
 * @author Bunny
 */
@FunctionalInterface
public interface SecurityRequirementRule {

	/**
	 * 匹配规则
	 * @param handlerMethod 处理器方法
	 * @return 命中时的安全要求；未命中为空
	 */
	Optional<SecurityRequirement> match(HandlerMethod handlerMethod);

}
