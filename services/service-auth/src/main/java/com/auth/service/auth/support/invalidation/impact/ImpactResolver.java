package com.auth.service.auth.support.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidatePayload;

import java.util.Set;

/**
 * 影响面解析策略：仅负责将类型化业务键解析为受影响用户 ID
 *
 * @param <T> 支持的 {@link AuthorizationInvalidatePayload} 子类型
 * @author Bunny
 */
public interface ImpactResolver<T extends AuthorizationInvalidatePayload> {

	/**
	 * 本解析器处理的变更维度
	 * @return 变更维度
	 */
	AuthorizationChangeKind kind();

	/**
	 * 支持的 Payload 类型（用于注册表分发时的类型检查）
	 * @return Payload Class
	 */
	Class<T> payloadType();

	/**
	 * 解析受影响用户 ID
	 * @param payload 类型化失效业务键
	 * @return 去重用户 ID
	 */
	Set<Long> resolve(T payload);

}
