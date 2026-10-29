package com.auth.service.auth.support.authorization;

import com.auth.module.security.contract.profile.ScopeGrant;
import com.auth.module.security.contract.profile.datascope.DataScopeDimension;
import com.auth.module.security.contract.profile.datascope.DataScopeStorageType;
import com.auth.service.auth.mapper.DeptClosureMapper;
import com.auth.service.auth.model.po.scope.DeptClosureDescendantRowPO;
import com.auth.service.auth.model.po.scope.RoleScopePO;
import com.auth.service.auth.model.po.scope.UserScopePO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DataScopeResolver} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DataScopeResolverTest {

	@Mock
	private DeptClosureMapper deptClosureMapper;

	@InjectMocks
	private DataScopeResolver resolver;

	private static UserScopePO userScope(String scopeType, String scopeIds) {
		UserScopePO projection = new UserScopePO();
		projection.setDimension("DEPT");
		projection.setScopeType(scopeType);
		projection.setScopeIds(scopeIds);
		return projection;
	}

	private static RoleScopePO roleScope(String roleCode, String scopeType, String scopeIds) {
		RoleScopePO roleScope = new RoleScopePO();
		roleScope.setRoleCode(roleCode);
		roleScope.setDimension("DEPT");
		roleScope.setScopeType(scopeType);
		roleScope.setScopeIds(scopeIds);
		return roleScope;
	}

	@Test
	@DisplayName("user_scope 存在时覆盖 role_scope 且类型与库表一致")
	void userScope_shouldOverrideRoleScope() {
		UserScopePO projection = userScope("IDS", "[1,2]");

		ScopeGrant grant = resolve(List.of("ADMIN"), Map.of(10L, List.of(projection)), List.of());

		assertEquals(DataScopeStorageType.IDS, grant.getScopeType());
		assertEquals(List.of(1L, 2L), grant.getValues());
	}

	@Test
	@DisplayName("user_scope 为 SELF 时仍走 SELF")
	void userScope_self_shouldStaySelf() {
		UserScopePO projection = userScope("SELF", null);

		ScopeGrant grant = resolve(List.of("ADMIN"), Map.of(10L, List.of(projection)), List.of());

		assertEquals(DataScopeStorageType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("user_scope 为 IDS_AND_CHILD 时展开闭包后仍为 IDS_AND_CHILD")
	void userScope_idsAndChild_shouldExpandClosure() {
		UserScopePO projection = userScope("IDS_AND_CHILD", "[10]");
		stubDescendantRows(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolve(List.of("R1"), Map.of(10L, List.of(projection)), List.of());

		assertEquals(DataScopeStorageType.IDS_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
		verify(deptClosureMapper).selectDescendantRowsByAncestorIds(anyList());
	}

	@Test
	@DisplayName("无 user_scope 时多角色 IDS merge 仍为 IDS")
	void roleScopes_shouldMergeAsIds() {
		RoleScopePO roleScope1 = roleScope("R1", "IDS", "[1,2]");
		RoleScopePO roleScope2 = roleScope("R2", "IDS", "[2,3]");

		ScopeGrant grant = resolve(List.of("R1", "R2"), Map.of(), List.of(roleScope1, roleScope2));

		assertEquals(DataScopeStorageType.IDS, grant.getScopeType());
		assertEquals(List.of(1L, 2L, 3L), grant.getValues());
	}

	@Test
	@DisplayName("无 user_scope 且无 role_scope 时不写入该维")
	void noScopeRows_shouldOmitDimension() {
		Map<DataScopeDimension, ScopeGrant> byDimension = resolver
			.resolveEffectiveGrants(List.of(10L), Map.of(10L, List.of("R1")), Map.of(), List.of())
			.get(10L);

		assertNull(byDimension.get(DataScopeDimension.DEPT));
	}

	@Test
	@DisplayName("IDS_AND_CHILD 展开闭包后仍为 IDS_AND_CHILD")
	void idsAndChild_shouldExpandClosure() {
		RoleScopePO roleScope = roleScope("R1", "IDS_AND_CHILD", "[10]");
		stubDescendantRows(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolve(List.of("R1"), Map.of(), List.of(roleScope));

		assertEquals(DataScopeStorageType.IDS_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
	}

	@Test
	@DisplayName("scope_ids 为空时 IDS 物化为空列表")
	void blankScopeIds_shouldMaterializeEmptyValues() {
		RoleScopePO roleScope = roleScope("R1", "IDS", null);

		ScopeGrant grant = resolve(List.of("R1"), Map.of(), List.of(roleScope));

		assertEquals(DataScopeStorageType.IDS, grant.getScopeType());
		assertEquals(List.of(), grant.getValues());
	}

	private ScopeGrant resolve(List<String> roleCodes, Map<Long, List<UserScopePO>> userScopes,
			List<RoleScopePO> roleScopes) {
		return resolver.resolveEffectiveGrants(List.of(10L), Map.of(10L, roleCodes), userScopes, roleScopes)
			.get(10L)
			.get(DataScopeDimension.DEPT);
	}

	private void stubDescendantRows(List<Long> descendantIds) {
		List<DeptClosureDescendantRowPO> rows = descendantIds.stream().map(descendantId -> {
			DeptClosureDescendantRowPO row = new DeptClosureDescendantRowPO();
			row.setAncestorId(10L);
			row.setDescendantId(descendantId);
			return row;
		}).toList();
		when(deptClosureMapper.selectDescendantRowsByAncestorIds(anyList())).thenReturn(rows);
	}

}
