package com.auth.module.security.autoconfigure.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.autoconfigure.config.security.AuditPolicy;
import com.auth.module.security.autoconfigure.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfigure.pipeline.resolver.HandlerMethodResolver;
import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.autoconfigure.security.SecurityRequirement;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import com.auth.module.security.core.matcher.PermissionMatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static com.auth.module.security.autoconfigure.web.SecurityUserUtils.currentAuthProfile;
import static com.auth.module.security.contract.constants.PermissionConstant.isAdmin;
import static com.auth.module.security.contract.constants.PermissionConstant.isAdminPermission;

/**
 * Permission decision service.
 *
 * @author Bunny
 */
@Component("auth")
public class PermissionService {

	private final ApplicationEventPublisher publisher;

	private final SecurityConfigProperties securityConfigProperties;

	private final List<AuthorizationAuditHandler> auditHandlers;

	private final ObjectProvider<HandlerMethodResolver> handlerMethodResolver;

	public PermissionService(ApplicationEventPublisher publisher, SecurityConfigProperties securityConfigProperties,
			List<AuthorizationAuditHandler> auditHandlers,
			ObjectProvider<HandlerMethodResolver> handlerMethodResolver) {
		this.publisher = publisher;
		this.securityConfigProperties = securityConfigProperties;
		this.auditHandlers = auditHandlers;
		this.handlerMethodResolver = handlerMethodResolver;
	}

	/**
	 * 从当前请求获取 {@link SecurityRequirement}
	 * @return 安全要求枚举
	 */
	private static SecurityRequirement currentSecurityRequirement() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			return null;
		}
		HttpServletRequest request = attrs.getRequest();
		Object raw = request.getAttribute(SecurityRequestAttributes.REQUIREMENT);
		return raw instanceof SecurityRequirement req ? req : null;
	}

	/**
	 * 解析客户端 IP（优先 X-Forwarded-For 首段）
	 * @param request 请求
	 * @return IP 或 null
	 */
	private static String resolveClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			String first = forwarded.split(",")[0].trim();
			if (CharSequenceUtil.isNotEmpty(first) && !CharSequenceUtil.equalsIgnoreCase(first, "unknown")) {
				return first;
			}
		}
		return request.getRemoteAddr();
	}

	/**
	 * 决定权限
	 * @param required 需要的权限
	 * @return 是否决定权限
	 */
	public boolean decide(String required) {
		AuthProfile profile = currentAuthProfile();
		if (profile == null || profile.getUserId() == null) {
			publishIfNeeded(SecurityEventType.DENIED, null, null, required, "UNAUTHENTICATED");
			return false;
		}

		Long userId = profile.getUserId();
		String username = profile.getUsername();
		List<String> roles = profile.getRoles();
		List<String> permissions = profile.getPermissions();

		// 是否是管理员Id、角色是否是管理员
		if (isAdmin(userId, roles)) {
			publishIfNeeded(SecurityEventType.GRANTED, userId, username, required,
					"BYPASS_SUPER_USER_ID_OR_BYPASS_ADMIN_ROLE");
			return true;
		}

		// 权限是否是管理员
		if (isAdminPermission(permissions)) {
			publishIfNeeded(SecurityEventType.GRANTED, userId, username, required, "BYPASS_GLOBAL_WILDCARD_PERMISSION");
			return true;
		}

		// 注解权限码是否匹配成功
		boolean isGranted = permissions.stream().anyMatch(granted -> PermissionMatcher.matches(granted, required));
		if (isGranted) {
			publishIfNeeded(SecurityEventType.GRANTED, userId, username, required, "MATCHED");
			return true;
		}

		publishIfNeeded(SecurityEventType.DENIED, userId, username, required, "ACCESS_DENIED");
		return false;
	}

	/**
	 * 按审计策略判断是否需要发布事件，然后 publish + 委托给 SPI 处理器
	 */
	private void publishIfNeeded(SecurityEventType type, Long userId, String username, String required, String reason) {
		if (!shouldAudit()) {
			return;
		}
		SecurityAuthorizationAuditPayloadEvent.SecurityAuthorizationAuditPayloadEventBuilder builder = SecurityAuthorizationAuditPayloadEvent
			.builder()
			.eventType(type)
			.userId(userId)
			.username(username)
			.requiredAuthority(required)
			.decisionReason(reason)
			.exceptionMessage(null);
		enrichAuditPayloadFromRequest(builder);
		SecurityAuthorizationAuditPayloadEvent event = builder.build();
		publisher.publishEvent(event);
		auditHandlers.forEach(h -> h.handle(event));
	}

	/**
	 * 从当前 Web 请求补充审计字段（HTTP、Handler 等）
	 * @param builder 事件构造器
	 */
	private void enrichAuditPayloadFromRequest(
			SecurityAuthorizationAuditPayloadEvent.SecurityAuthorizationAuditPayloadEventBuilder builder) {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
		if (request == null) {
			return;
		}
		builder.requestMethod(request.getMethod());
		builder.requestUri(request.getRequestURI());
		builder.requestIp(resolveClientIp(request));
		Optional.ofNullable(handlerMethodResolver.getIfAvailable())
			.flatMap(resolver -> resolver.resolve(request))
			.ifPresent(handlerMethod -> builder.className(handlerMethod.getBeanType().getName())
				.methodName(handlerMethod.getMethod().getName()));
	}

	/**
	 * 根据 {@link AuditPolicy} 判断当前请求是否需要记录审计
	 */
	private boolean shouldAudit() {
		AuditPolicy policy = securityConfigProperties.getAuditPolicy();
		if (policy == null) {
			policy = AuditPolicy.PUBLIC_WITH_TOKEN_RECORD;
		}
		return switch (policy) {
			case NONE -> false;
			case ALL_RECORD -> true;
			case PUBLIC_NO_RECORD -> {
				SecurityRequirement requirement = currentSecurityRequirement();
				yield requirement != SecurityRequirement.PUBLIC;
			}
			// 能走到 decide() 一定有 token，因此等价于全部记录
			case PUBLIC_WITH_TOKEN_RECORD -> true;
		};
	}

}
