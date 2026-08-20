package com.auth.module.security.autoconfigure.security;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfigure.pipeline.filter.TokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.auth.module.security.autoconfigure.security.AnnotationOverridingAuthorizationSupport.isNonAnonymousAuthenticated;
import static com.auth.module.security.autoconfigure.security.AnnotationOverridingAuthorizationSupport.toPathMatchers;

/**
 * 在 HTTP 授权阶段
 *
 * @author Bunny
 * @see TokenAuthenticationFilter
 */
public class AnnotationOverridingAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	private final SecurityConfigProperties securityProperties;

	private final Object matcherCacheLock = new Object();

	@Nullable
	private List<String> permitPathsIdentity;

	private List<RequestMatcher> permitMatchersCache = List.of();

	public AnnotationOverridingAuthorizationManager(SecurityConfigProperties securityProperties) {
		this.securityProperties = Objects.requireNonNull(securityProperties, "securityProperties");
	}

	/**
	 * 获取当前的 permit matchers
	 * @return 当前的 permit matchers
	 */
	private List<RequestMatcher> currentPermitMatchers() {
		List<String> paths = this.securityProperties.getPermitPaths();
		paths = CollUtil.emptyIfNull(paths);

		synchronized (this.matcherCacheLock) {
			if (paths != this.permitPathsIdentity) {
				this.permitPathsIdentity = paths;
				this.permitMatchersCache = toPathMatchers(paths);
			}
			return this.permitMatchersCache;
		}
	}

	@Override
	@Nullable
	public AuthorizationResult authorize(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
		HttpServletRequest request = context.getRequest();
		Object raw = request.getAttribute(SecurityRequestAttributes.REQUIREMENT);
		if (raw instanceof SecurityRequirement requirement) {
			if (requirement == SecurityRequirement.PUBLIC) {
				return new AuthorizationDecision(true);
			}
			if (requirement == SecurityRequirement.AUTHENTICATED) {
				return new AuthorizationDecision(isNonAnonymousAuthenticated(authentication.get()));
			}
			// FALLBACK_TO_PATH：继续走路径规则
		}

		boolean matchesAny = CollUtil.emptyIfNull(currentPermitMatchers())
			.stream()
			.anyMatch(matcher -> matcher.matches(request));
		if (matchesAny) {
			return new AuthorizationDecision(true);
		}

		// 默认需认证
		return new AuthorizationDecision(isNonAnonymousAuthenticated(authentication.get()));
	}

	/**
	 * 委托 {@link #authorize(Supplier, RequestAuthorizationContext)}；保留以满足当前
	 * {@link AuthorizationManager} 对抽象 check 的编译要求 NOSONAR S1133 — Spring Security 6.5
	 * 仍声明抽象 check，需保留桥接
	 */
	@Override
	public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
		AuthorizationResult r = authorize(authentication, context);
		if (r instanceof AuthorizationDecision ad) {
			return ad;
		}
		return new AuthorizationDecision(r != null && r.isGranted());
	}

}
