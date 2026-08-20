package com.auth.module.security.contract.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 操作日志负载（契约层），由切面组装后交给 {@link com.auth.module.security.contract.spi.OperationLogHandler}。
 *
 * @author Bunny
 */
@Value
@Builder
public class OperationLogPayloadEvent {

	/**
	 * 操作用户 ID，未登录可为 null，持久化层应规范为 0。
	 */
	Long userId;

	/**
	 * 操作用户名快照；未登录可为 null。
	 */
	String username;

	/**
	 * 操作类型枚举名
	 */
	String operationType;

	/**
	 * 模块键：由 {@link com.auth.module.security.contract.api.audit.AuditOperationModuleKeys}
	 * 将大模块与小模块组合， 形如 SYSTEM:SYS_DEPT，写入 log_operation.module。
	 */
	String module;

	/**
	 * 目标类型
	 */
	String targetType;

	/**
	 * 业务目标主键；未通过 {@link com.auth.module.security.contract.context.OperationLogContext}
	 * 设置时为 null
	 */
	Long targetId;

	/**
	 * HTTP 方法
	 */
	String requestMethod;

	/**
	 * 请求 URI
	 */
	String requestUri;

	/**
	 * 请求参数摘要
	 */
	String requestParams;

	/**
	 * HTTP 或业务层感知的响应状态码
	 */
	Integer responseStatus;

	/**
	 * 响应消息摘要
	 */
	String responseMessage;

	/**
	 * 执行耗时毫秒
	 */
	Integer executionTimeMs;

	/**
	 * 客户端 IP
	 */
	String ipAddress;

	/**
	 * User-Agent
	 */
	String userAgent;

	/**
	 * 控制器类名
	 */
	String className;

	/**
	 * 控制器方法名
	 */
	String methodName;

	/**
	 * 记录时间
	 */
	@Builder.Default
	Instant timestamp = Instant.now();

}
