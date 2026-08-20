package com.auth.module.security.contract.dto;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * 操作日志入库请求
 *
 * @author Bunny
 */
@Getter
@Setter
public class OperationLogIngestRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;

	private String username;

	private String operationType;

	private String module;

	private String targetType;

	private Long targetId;

	private String requestMethod;

	private String requestUri;

	private String requestParams;

	private Integer responseStatus;

	private String responseMessage;

	private Integer executionTimeMs;

	private String ipAddress;

	private String userAgent;

	private String className;

	private String methodName;

	private Instant timestamp;

	/**
	 * 从操作日志负载构建入库请求
	 * @param payload 操作日志负载
	 * @return 入库请求
	 */
	public static OperationLogIngestRequest fromPayload(OperationLogPayloadEvent payload) {
		OperationLogIngestRequest request = new OperationLogIngestRequest();
		request.setUserId(payload.getUserId());
		request.setUsername(payload.getUsername());
		request.setOperationType(payload.getOperationType());
		request.setModule(payload.getModule());
		request.setTargetType(payload.getTargetType());
		request.setTargetId(payload.getTargetId());
		request.setRequestMethod(payload.getRequestMethod());
		request.setRequestUri(payload.getRequestUri());
		request.setRequestParams(payload.getRequestParams());
		request.setResponseStatus(payload.getResponseStatus());
		request.setResponseMessage(payload.getResponseMessage());
		request.setExecutionTimeMs(payload.getExecutionTimeMs());
		request.setIpAddress(payload.getIpAddress());
		request.setUserAgent(payload.getUserAgent());
		request.setClassName(payload.getClassName());
		request.setMethodName(payload.getMethodName());
		request.setTimestamp(payload.getTimestamp());
		return request;
	}

	/**
	 * 转换为操作日志负载
	 * @return 操作日志负载
	 */
	public OperationLogPayloadEvent toPayload() {
		Instant ts = Objects.requireNonNullElseGet(this.timestamp, Instant::now);
		return OperationLogPayloadEvent.builder()
			.userId(this.userId)
			.username(this.username)
			.operationType(this.operationType)
			.module(this.module)
			.targetType(this.targetType)
			.targetId(this.targetId)
			.requestMethod(this.requestMethod)
			.requestUri(this.requestUri)
			.requestParams(this.requestParams)
			.responseStatus(this.responseStatus)
			.responseMessage(this.responseMessage)
			.executionTimeMs(this.executionTimeMs)
			.ipAddress(this.ipAddress)
			.userAgent(this.userAgent)
			.className(this.className)
			.methodName(this.methodName)
			.timestamp(ts)
			.build();
	}

}
