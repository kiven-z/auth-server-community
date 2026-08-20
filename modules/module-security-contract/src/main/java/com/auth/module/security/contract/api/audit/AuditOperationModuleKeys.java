package com.auth.module.security.contract.api.audit;

import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * 将 {@link AuditServiceDomain} 与小模块编码组合为写入 log_operation.module 的稳定键。
 * <p>
 * 跨能力共享资源码见 {@link PlatformBizCodes}；服务域专有编码由各微服务本地常量维护。平台仅负责拼接规则。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class AuditOperationModuleKeys {

	/**
	 * 复合键分隔符（大模块:小模块）
	 */
	public static final char SEPARATOR = ':';

	/**
	 * 生成持久化用模块键，形如 SYSTEM:SYS_DEPT。
	 * @param domain 大模块（微服务域）
	 * @param bizModuleCode 小模块编码，非空白（由各服务常量类提供）
	 * @return 非空复合键
	 */
	public static String toPersistedModuleKey(AuditServiceDomain domain, String bizModuleCode) {
		Objects.requireNonNull(domain, "domain");
		Objects.requireNonNull(bizModuleCode, "bizModuleCode");
		String trimmed = bizModuleCode.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("bizModuleCode must not be blank");
		}
		return domain.name() + SEPARATOR + trimmed;
	}

}
