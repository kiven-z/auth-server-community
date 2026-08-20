package com.auth.service.auth.support.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.PermissionInvalidatePayload;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * {@link ImpactResolverRegistry} 单元测试
 */
@DisplayName("ImpactResolverRegistry 影响面分发")
@ExtendWith(MockitoExtension.class)
class ImpactResolverRegistryTest {

	@Mock
	private AuthorizationImpactQuerySupport impactQuery;

	@Test
	@DisplayName("按 Payload kind 分发到对应解析器")
	void resolve_shouldDispatchByPayloadKind() {
		when(impactQuery.findUserIdsByRoleCodes(List.of("ADMIN"))).thenReturn(Set.of(1L));

		ImpactResolver<RoleInvalidatePayload> roleResolver = new PortBackedImpactResolver<>(
				AuthorizationChangeKind.ROLE, RoleInvalidatePayload.class,
				payload -> impactQuery.findUserIdsByRoleCodes(payload.roleCodes()));
		ImpactResolverRegistry registry = new ImpactResolverRegistry(List.of(roleResolver));

		Set<Long> userIds = registry.resolve(new RoleInvalidatePayload(List.of("ADMIN")));

		assertEquals(Set.of(1L), userIds);
	}

	@Test
	@DisplayName("未注册 kind 时抛出 IllegalStateException")
	void resolve_unregisteredKind_shouldThrow() {
		ImpactResolver<RoleInvalidatePayload> roleResolver = new PortBackedImpactResolver<>(
				AuthorizationChangeKind.ROLE, RoleInvalidatePayload.class,
				payload -> impactQuery.findUserIdsByRoleCodes(payload.roleCodes()));
		ImpactResolverRegistry registry = new ImpactResolverRegistry(List.of(roleResolver));

		PermissionInvalidatePayload unregisteredPayload = new PermissionInvalidatePayload(List.of("sys:user:list"));
		assertThrows(IllegalStateException.class, () -> registry.resolve(unregisteredPayload));
	}

}
