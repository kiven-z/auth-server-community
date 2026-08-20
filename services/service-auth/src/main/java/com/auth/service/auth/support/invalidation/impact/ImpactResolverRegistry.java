package com.auth.service.auth.support.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidatePayload;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 影响面解析器注册表：按 {@link AuthorizationChangeKind} 分发到对应 {@link ImpactResolver}
 *
 * @author Bunny
 */
@Component
public class ImpactResolverRegistry {

	private final Map<AuthorizationChangeKind, ImpactResolver<?>> resolversByKind;

	/**
	 * 注入全部解析器并建立 kind → resolver 映射
	 * @param resolvers Spring 容器中的解析器列表
	 */
	public ImpactResolverRegistry(List<ImpactResolver<?>> resolvers) {
		Map<AuthorizationChangeKind, ImpactResolver<?>> map = new EnumMap<>(AuthorizationChangeKind.class);

		for (ImpactResolver<?> resolver : resolvers) {
			ImpactResolver<?> previous = map.putIfAbsent(resolver.kind(), resolver);
			if (previous != null) {
				throw new IllegalStateException("存在重复的 ImpactResolver，变更类型为 " + resolver.kind() + "，分别为 "
						+ previous.getClass().getName() + " 与 " + resolver.getClass().getName());

			}
		}

		this.resolversByKind = Map.copyOf(map);
	}

	/**
	 * 将类型化失效 Payload 解析为受影响用户 ID 集合
	 * @param payload 失效业务键
	 * @return 去重用户 ID
	 */
	public Set<Long> resolve(AuthorizationInvalidatePayload payload) {
		if (payload == null) {
			throw new IllegalArgumentException("payload不能为空");
		}

		ImpactResolver<?> resolver = resolversByKind.get(payload.kind());
		if (resolver == null) {
			throw new IllegalStateException("不存在对应的 ImpactResolver，变更类型为 " + payload.kind());
		}

		return dispatch(resolver, payload);
	}

	/**
	 * 分发解析器
	 * @param resolver 解析器
	 * @param payload 失效业务键
	 * @return 受影响用户 ID 集合
	 */
	private <T extends AuthorizationInvalidatePayload> Set<Long> dispatch(ImpactResolver<T> resolver,
			AuthorizationInvalidatePayload payload) {
		Class<T> payloadType = resolver.payloadType();
		if (!payloadType.isInstance(payload)) {
			throw new IllegalArgumentException("payload类型不匹配，变更类型为 " + payload.kind() + "，分别为 "
					+ payloadType.getSimpleName() + " 与 " + payload.getClass().getName());
		}

		return resolver.resolve(payloadType.cast(payload));
	}

}
