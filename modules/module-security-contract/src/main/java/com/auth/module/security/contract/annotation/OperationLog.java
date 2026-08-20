package com.auth.module.security.contract.annotation;

import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;

import java.lang.annotation.*;

/**
 * 标记需要写入操作日志的 Web 控制器方法（或类上默认，方法可覆盖）
 *
 * @author Bunny
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

	/**
	 * 大模块（如 system、auth、example）
	 * @return 审计服务域
	 */
	AuditServiceDomain serviceDomain();

	/**
	 * 小模块编码：跨能力共享资源用
	 * {@link com.auth.module.security.contract.api.audit.PlatformBizCodes}，
	 * 服务域专有编码用本服务本地常量
	 * <p>
	 * 建议使用 UPPER_SNAKE，与
	 * {@link com.auth.module.security.contract.api.audit.AuditOperationModuleKeys} 落库后缀一致
	 * </p>
	 * @return 业务模块码，如 "SYS_DEPT"
	 */
	String bizModule();

	/**
	 * 操作类型
	 * @return 操作类型
	 */
	OperationLogKind operation();

	/**
	 * 操作对象类型，如 User、Dept
	 * @return 类型文案，可为空
	 */
	String targetType() default "";

	/**
	 * 是否记录查询参数与表单参数（脱敏由落库方策略控制，此处仅做长度截断）
	 * @return 默认记录
	 */
	boolean recordParams() default true;

	/**
	 * 当返回值为统一 Result 包装类型时是否写入其 message 至 response_message
	 * @return 默认写入
	 */
	boolean recordResultMessage() default true;

}
