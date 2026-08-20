package com.auth.module.security.autoconfigure.audit.operationlog;

import com.auth.module.security.contract.annotation.OperationLog;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

/**
 * 从 AOP 连接点解析 {@link OperationLog} 元数据（方法优先，其次类级别）。
 *
 * @author Bunny
 */
@UtilityClass
public class OperationLogAnnotationResolver {

	/**
	 * 解析 {@link OperationLog}：先取目标方法（含 CGLIB 场景），再回退到目标类上的类级别注解。
	 * @param joinPoint 当前连接点
	 * @return 注解实例；无法解析时 null
	 */
	public static OperationLog resolve(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		Object target = joinPoint.getTarget();
		Class<?> targetClass = target != null ? target.getClass() : method.getDeclaringClass();
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
		OperationLog fromMethod = AnnotationUtils.findAnnotation(specificMethod, OperationLog.class);
		if (fromMethod != null) {
			return fromMethod;
		}
		return AnnotationUtils.findAnnotation(targetClass, OperationLog.class);
	}

}
