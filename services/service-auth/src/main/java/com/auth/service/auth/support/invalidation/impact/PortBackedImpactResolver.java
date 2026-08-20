package com.auth.service.auth.support.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidatePayload;

import java.util.Set;
import java.util.function.Function;

/**
 * 通过委托函数将类型化失效 Payload 解析为受影响用户 ID。
 *
 * @param <T> 支持的 {@link AuthorizationInvalidatePayload} 子类型
 * @author Bunny
 */
public final class PortBackedImpactResolver<T extends AuthorizationInvalidatePayload> implements ImpactResolver<T> {

	private final AuthorizationChangeKind kind;

	private final Class<T> payloadType;

	private final Function<T, Set<Long>> query;

	/**
	 * @param kind 变更维度
	 * @param payloadType Payload 类型
	 * @param query 影响面反查逻辑
	 */
	public PortBackedImpactResolver(AuthorizationChangeKind kind, Class<T> payloadType, Function<T, Set<Long>> query) {
		this.kind = kind;
		this.payloadType = payloadType;
		this.query = query;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationChangeKind kind() {
		return kind;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> payloadType() {
		return payloadType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> resolve(T payload) {
		return query.apply(payload);
	}

}
