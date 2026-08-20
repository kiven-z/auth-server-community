package com.auth.module.security.autoconfigure.pipeline.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import java.util.Optional;

/**
 * 从当前请求解析 MVC {@link HandlerMethod}（用于注解决策）
 *
 * <p>
 * 在 Spring Security 过滤器链中执行时，请求通常尚未经过 DispatcherServlet，因此可能尚未缓存
 * {@link org.springframework.http.server.RequestPath}{@link RequestMappingHandlerMapping#getHandler}
 * 在 PathPattern 模式下依赖该缓存，故此处在解析前按需 {@link ServletRequestPathUtils#parseAndCache}，并在本方法结束时
 * 若由本方法创建缓存则 {@link ServletRequestPathUtils#clearParsedRequestPath}，避免干扰后续 Servlet 正常解析
 * </p>
 *
 * @author Bunny
 */
@Slf4j
public final class HandlerMethodResolver {

	private final RequestMappingHandlerMapping requestMappingHandlerMapping;

	public HandlerMethodResolver(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		this.requestMappingHandlerMapping = requestMappingHandlerMapping;
	}

	/**
	 * 解析处理器方法
	 * @param request HTTP 请求
	 * @return 处理器方法；无法解析时为空
	 */
	public Optional<HandlerMethod> resolve(HttpServletRequest request) {
		boolean cleanupRequired = false;
		try {
			if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
				ServletRequestPathUtils.parseAndCache(request);
				cleanupRequired = true;
			}
			try {
				HandlerExecutionChain chain = requestMappingHandlerMapping.getHandler(request);
				if (chain != null) {
					Object handler = chain.getHandler();
					if (handler instanceof HandlerMethod hm) {
						return Optional.of(hm);
					}
				}
			}
			catch (Exception exception) {
				log.debug("Resolve handler method failed: {}", exception.getMessage());
			}
			return Optional.empty();
		}
		finally {
			if (cleanupRequired) {
				ServletRequestPathUtils.clearParsedRequestPath(request);
			}
		}
	}

}
