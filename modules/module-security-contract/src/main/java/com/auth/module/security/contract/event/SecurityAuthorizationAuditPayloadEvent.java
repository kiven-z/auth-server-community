package com.auth.module.security.contract.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * 授权审计负载（契约层） 注意：不要将原始令牌/密码/cookie 放入任何字段
 *
 * @author Bunny
 */
@Value
@Builder
public class SecurityAuthorizationAuditPayloadEvent {

	/**
	 * 事件类型
	 */
	SecurityEventType eventType;

	/**
	 * 用户 ID
	 */
	Long userId;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 请求 IP
	 */
	String requestIp;

	/**
	 * 请求方法
	 */
	String requestMethod;

	/**
	 * 请求 URI
	 */
	String requestUri;

	/**
	 * 所需权限
	 */
	String requiredAuthority;

	/**
	 * 决策原因
	 */
	String decisionReason;

	/**
	 * 异常消息
	 */
	String exceptionMessage;

	/**
	 * 类名
	 */
	String className;

	/**
	 * 方法名
	 */
	String methodName;

	/**
	 * 方法参数
	 */
	@Builder.Default
	Map<String, Object> methodParams = Collections.emptyMap();

	/**
	 * 时间戳
	 */
	@Builder.Default
	Instant timestamp = Instant.now();

}
