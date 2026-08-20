package com.auth.module.security.autoconfigure.audit.operationlog;

import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.context.OperationLogContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 在带 {@link OperationLog} 的 Web 控制器方法周围委托 {@link OperationLogPayloadAssembler} 发布审计负载。
 *
 * @author Bunny
 */
@Aspect
public class OperationLogAspect {

	private final OperationLogPayloadAssembler payloadAssembler;

	public OperationLogAspect(OperationLogPayloadAssembler payloadAssembler) {
		this.payloadAssembler = payloadAssembler;
	}

	/**
	 * 使用全限定名绑定，避免 @annotation(x)||@within(x) 对同一形参绑定在 Spring AOP 下出现 null。
	 */
	@Around("@annotation(com.auth.module.security.contract.annotation.OperationLog) "
			+ "|| @within(com.auth.module.security.contract.annotation.OperationLog)")
	public Object aroundControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		long startNs = System.nanoTime();
		Object result = null;
		Throwable failure = null;
		try {
			result = joinPoint.proceed();
			return result;
		}
		catch (Throwable ex) {
			failure = ex;
			throw ex;
		}
		finally {
			OperationLog meta = OperationLogAnnotationResolver.resolve(joinPoint);
			try {
				payloadAssembler.assembleAndPublish(joinPoint, meta, result, failure, startNs);
			}
			finally {
				OperationLogContext.clear();
			}
		}
	}

}
