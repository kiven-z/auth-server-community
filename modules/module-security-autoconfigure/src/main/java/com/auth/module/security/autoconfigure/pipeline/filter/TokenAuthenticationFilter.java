package com.auth.module.security.autoconfigure.pipeline.filter;

import com.auth.module.security.autoconfigure.pipeline.authenticate.SecurityAuthExecutor;
import com.auth.module.security.autoconfigure.pipeline.resolver.SecurityRequirementResolver;
import com.auth.module.security.autoconfigure.security.AuthAction;
import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import com.auth.module.security.autoconfigure.web.error.SecurityErrorResponseSupport;
import com.auth.module.security.contract.exception.SecurityTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 资源服务器认证过滤器：注解优先分派 + 委托认证执行器
 *
 * @author Bunny
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

	/**
	 * 需求解析器
	 */
	private final SecurityRequirementResolver requirementResolver;

	/**
	 * 认证执行器
	 */
	private final SecurityAuthExecutor authExecutor;

	/**
	 * 分派表
	 */
	private final Map<SecurityRequirement, AuthAction> dispatch;

	public TokenAuthenticationFilter(SecurityRequirementResolver requirementResolver, SecurityAuthExecutor authExecutor,
			Map<SecurityRequirement, AuthAction> dispatch) {
		this.requirementResolver = requirementResolver;
		this.authExecutor = authExecutor;
		this.dispatch = dispatch;
	}

	/**
	 * 过滤内部实现
	 * @param request HTTP 请求
	 * @param response HTTP 响应
	 * @param filterChain 过滤器链
	 * @throws ServletException 异常
	 * @throws IOException 异常
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		// 先清空上下文
		SecurityContextHolder.clearContext();
		try {
			// 解析当前
			SecurityRequirement requirement = requirementResolver.resolve(request);
			request.setAttribute(SecurityRequestAttributes.REQUIREMENT, requirement);

			AuthAction action = dispatch.getOrDefault(requirement, AuthAction.TRY);
			action.execute(authExecutor, request);
			filterChain.doFilter(request, response);
		}
		catch (SecurityTokenException ex) {
			request.setAttribute(SecurityRequestAttributes.SECURITY_ERROR, ex.getCode());
			SecurityErrorResponseSupport.write(request, response, ex.getCode());
		}
	}

}
