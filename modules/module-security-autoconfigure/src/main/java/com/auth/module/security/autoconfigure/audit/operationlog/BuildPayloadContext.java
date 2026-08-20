package com.auth.module.security.autoconfigure.audit.operationlog;

import com.auth.module.security.contract.annotation.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 构建操作日志所需全部上下文。
 *
 * @author Bunny
 */
public record BuildPayloadContext(OperationLog meta, Object result, Throwable failure, long startNs,
		HttpServletRequest request, HttpServletResponse response, Method method, List<Object> methodArgs) {
}