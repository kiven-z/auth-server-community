package com.auth.service.auth.support.invalidation.impact;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.module.security.contract.dto.invalidation.UserInvalidatePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PortBackedImpactResolver} 单元测试
 */
@DisplayName("PortBackedImpactResolver 影响面解析")
@ExtendWith(MockitoExtension.class)
class PortBackedImpactResolverTest {

	@Mock
	private AuthorizationImpactQuerySupport impactQuery;

	@Test
	@DisplayName("委托查询：按角色码反查用户 ID")
	void resolve_delegated_shouldDelegateToImpactQuery() {
		when(impactQuery.findUserIdsByRoleCodes(List.of("R_A", "R_B"))).thenReturn(Set.of(10L, 20L));

		ImpactResolver<RoleInvalidatePayload> resolver = new PortBackedImpactResolver<>(AuthorizationChangeKind.ROLE,
				RoleInvalidatePayload.class, payload -> impactQuery.findUserIdsByRoleCodes(payload.roleCodes()));
		Set<Long> userIds = resolver.resolve(new RoleInvalidatePayload(List.of("R_A", "R_B")));

		assertEquals(Set.of(10L, 20L), userIds);
		verify(impactQuery).findUserIdsByRoleCodes(List.of("R_A", "R_B"));
	}

	@Test
	@DisplayName("直连：按 payload 中的 userIds 返回影响面")
	void resolve_userPayload_shouldReturnUserIds() {
		ImpactResolver<UserInvalidatePayload> resolver = new PortBackedImpactResolver<>(AuthorizationChangeKind.USER,
				UserInvalidatePayload.class, payload -> Set.copyOf(payload.userIds()));

		Set<Long> impacted = resolver.resolve(new UserInvalidatePayload(List.of(1L, 2L)));

		assertEquals(Set.of(1L, 2L), impacted);
	}

	@Test
	@DisplayName("元数据：ROLE 维度与 RoleInvalidatePayload 类型")
	void metadata_shouldMatchRoleKind() {
		ImpactResolver<RoleInvalidatePayload> resolver = new PortBackedImpactResolver<>(AuthorizationChangeKind.ROLE,
				RoleInvalidatePayload.class, payload -> impactQuery.findUserIdsByRoleCodes(payload.roleCodes()));

		assertEquals(AuthorizationChangeKind.ROLE, resolver.kind());
		assertEquals(RoleInvalidatePayload.class, resolver.payloadType());
	}

}
