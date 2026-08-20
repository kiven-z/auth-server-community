package com.auth.module.security.contract.context;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import lombok.experimental.UtilityClass;

/**
 * 操作日志「业务目标主键」请求线程上下文。
 * <p>
 * 在业务代码中按需调用 {@link #setTargetId(Long)}，由 OperationLogAspect 在组装
 * {@link OperationLogPayloadEvent} 时读取；未设置则落库为 NULL。 切面在请求结束后会
 * {@link #clear()}，避免线程池复用导致串数据。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class OperationLogContext {

	private static final ThreadLocal<Long> TARGET_ID = new ThreadLocal<>();

	/**
	 * @return 当前线程已设置的目标主键；未设置时为 null
	 */
	public static Long getTargetId() {
		return TARGET_ID.get();
	}

	/**
	 * 设置当前请求线程关联的操作目标主键。
	 * @param id 业务主键，可为 null 表示清空线程内值
	 */
	public static void setTargetId(Long id) {
		if (id == null) {
			TARGET_ID.remove();
		}
		else {
			TARGET_ID.set(id);
		}
	}

	/**
	 * 移除当前线程的上下文，应在请求边界调用（由切面统一处理即可）。
	 */
	public static void clear() {
		TARGET_ID.remove();
	}

}
