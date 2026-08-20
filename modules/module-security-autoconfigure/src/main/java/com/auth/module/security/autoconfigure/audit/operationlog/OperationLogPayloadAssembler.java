package com.auth.module.security.autoconfigure.audit.operationlog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditOperationModuleKeys;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 将 Web 请求上下文、控制器方法实参与 {@link OperationLog} 元数据组装为 {@link OperationLogPayloadEvent} 并委托
 * {@link OperationLogHandler}。
 *
 * @author Bunny
 */
@Slf4j
public class OperationLogPayloadAssembler {

	private final OperationLogHandler operationLogHandler;

	private final ObjectMapper objectMapper;

	private final IpAddressService ipAddressService;

	/**
	 * @param operationLogHandler 可为 null 为 null 时跳过发布）
	 * @param objectMapper JSON 序列化
	 * @param ipAddressService IP 解析
	 */
	public OperationLogPayloadAssembler(OperationLogHandler operationLogHandler, ObjectMapper objectMapper,
			IpAddressService ipAddressService) {
		this.operationLogHandler = operationLogHandler;
		this.objectMapper = objectMapper;
		this.ipAddressService = ipAddressService;
	}

	/**
	 * 获取当前请求属性
	 * @return 当前请求属性
	 */
	private static ServletRequestAttributes currentRequestAttributes() {
		try {
			return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		}
		catch (IllegalStateException ex) {
			return null;
		}
	}

	/**
	 * 解析HTTP状态码
	 * @param response 响应
	 * @param failure 方法抛出的异常
	 * @return HTTP状态码
	 */
	private static Integer resolveHttpStatus(HttpServletResponse response, Throwable failure) {
		if (response == null) {
			return failure != null ? 500 : null;
		}
		int status = response.getStatus();
		return (failure != null && status < 400) ? 500 : status;
	}

	/**
	 * 组装并发布操作日志负载。
	 * @param joinPoint 连接点
	 * @param meta 注解元数据
	 * @param result 方法返回值
	 * @param failure 方法抛出的异常
	 * @param startNs 方法开始时间（纳秒）
	 */
	public void assembleAndPublish(ProceedingJoinPoint joinPoint, OperationLog meta, Object result, Throwable failure,
			long startNs) {
		if (operationLogHandler == null) {
			return;
		}
		if (meta == null) {
			log.warn("OperationLog metadata unresolved, skip publishing. target={}",
					joinPoint.getTarget() != null ? joinPoint.getTarget().getClass().getName() : "null");
			return;
		}
		ServletRequestAttributes attributes = currentRequestAttributes();
		if (attributes == null) {
			return;
		}

		HttpServletRequest request = attributes.getRequest();
		HttpServletResponse response = attributes.getResponse();
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		Object[] args = joinPoint.getArgs();
		// 可选 @RequestBody 可能为 null，不能用 List.copyOf
		List<Object> methodArgs = ArrayUtil.isEmpty(args) ? List.of() : new ArrayList<>(Arrays.asList(args));
		OperationLogPayloadEvent payload = buildPayload(
				new BuildPayloadContext(meta, result, failure, startNs, request, response, method, methodArgs));
		dispatch(payload);
	}

	/**
	 * 构建操作日志负载
	 * @param ctx 构建上下文
	 * @return 操作日志负载
	 */
	private OperationLogPayloadEvent buildPayload(BuildPayloadContext ctx) {
		// 解析执行时间
		int executionMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ctx.startNs());
		// 解析HTTP状态码
		Integer httpStatus = resolveHttpStatus(ctx.response(), ctx.failure());
		// 解析响应消息
		String responseMessage = OperationLogResponseMessageComposer.compose(ctx.meta(), ctx.result(), ctx.failure());
		// 解析用户ID
		Long userId = SecurityUserUtils.getUserId();
		// 解析用户名快照
		String username = SecurityUserUtils.getUsername();
		// 解析用户代理
		String userAgent = CharSequenceUtil.subPre(CharSequenceUtil.nullToEmpty(ctx.request().getHeader("User-Agent")),
				255);
		// 解析IP信息
		IpInfo ipInfo = ipAddressService.resolveIpInfo(ctx.request());

		// 构建操作日志负载
		return OperationLogPayloadEvent.builder()
			.userId(userId)
			.username(username)
			.operationType(ctx.meta().operation().name())
			// 解析模块
			.module(AuditOperationModuleKeys.toPersistedModuleKey(ctx.meta().serviceDomain(), ctx.meta().bizModule()))
			// 解析目标类型
			.targetType(CharSequenceUtil.nullToEmpty(ctx.meta().targetType()))
			// 解析目标ID
			.targetId(OperationLogContext.getTargetId())
			.requestMethod(ctx.request().getMethod())
			// 解析请求URI
			.requestUri(CharSequenceUtil.nullToEmpty(ctx.request().getRequestURI()))
			// 解析请求参数
			.requestParams(OperationLogRequestParamsBuilder.build(ctx.request(), ctx.meta().recordParams(),
					objectMapper, ctx.methodArgs()))
			// 解析响应状态码
			.responseStatus(httpStatus)
			// 解析响应消息
			.responseMessage(responseMessage)
			.executionTimeMs(executionMs)
			// 解析IP地址
			.ipAddress(ipInfo.getIpAddr())
			// 解析用户代理
			.userAgent(userAgent)
			// 解析类名
			.className(ctx.method().getDeclaringClass().getName())
			// 解析方法名
			.methodName(ctx.method().getName())
			.build();
	}

	/**
	 * 分发操作日志负载
	 * @param payload 操作日志负载
	 */
	private void dispatch(OperationLogPayloadEvent payload) {
		try {
			operationLogHandler.handle(payload);
		}
		catch (RuntimeException ex) {
			log.error("OperationLogHandler failed, uri={}, module={}", payload.getRequestUri(), payload.getModule(),
					ex);
		}
	}

}
