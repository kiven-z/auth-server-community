package com.auth.service.auth.support.authorization;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DeptDataScopeResolver} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DeptDataScopeResolverTest {

	@Mock
	private DeptClosureMapper deptClosureMapper;

	@InjectMocks
	private DeptDataScopeResolver resolver;

	private static UserScopePO userScope(String scopeType, String scopeDeptIds) {
		UserScopePO projection = new UserScopePO();
		projection.setScopeType(scopeType);
		projection.setScopeDeptIds(scopeDeptIds);
		return projection;
	}

	private static RoleScopePO roleScope(String roleCode, String scopeType, String scopeDeptIds) {
		RoleScopePO roleScope = new RoleScopePO();
		roleScope.setRoleCode(roleCode);
		roleScope.setScopeType(scopeType);
		roleScope.setScopeDeptIds(scopeDeptIds);
		return roleScope;
	}

	@Test
	@DisplayName("user_scope 存在时覆盖 role_scope 且类型与库表一致")
	void userScope_shouldOverrideRoleScope() {
		UserScopePO projection = userScope("DEPT", "[1,2]");

		ScopeGrant grant = resolve(List.of("ADMIN"), Map.of(10L, projection), List.of());

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(1L, 2L), grant.getValues());
	}

	@Test
	@DisplayName("user_scope 为 SELF 时仍走 SELF")
	void userScope_self_shouldStaySelf() {
		UserScopePO projection = userScope("SELF", null);

		ScopeGrant grant = resolve(List.of("ADMIN"), Map.of(10L, projection), List.of());

		assertEquals(DataScopeStorageType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("user_scope 为 DEPT_AND_CHILD 时展开闭包后仍为 DEPT_AND_CHILD")
	void userScope_deptAndChild_shouldExpandClosure() {
		UserScopePO projection = userScope("DEPT_AND_CHILD", "[10]");
		stubDescendantRows(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolve(List.of("R1"), Map.of(10L, projection), List.of());

		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
		verify(deptClosureMapper).selectDescendantRowsByAncestorIds(anyList());
	}

	@Test
	@DisplayName("无 user_scope 时多角色 DEPT merge 仍为 DEPT")
	void roleScopes_shouldMergeAsDept() {
		RoleScopePO roleScope1 = roleScope("R1", "DEPT", "[1,2]");
		RoleScopePO roleScope2 = roleScope("R2", "DEPT", "[2,3]");

		ScopeGrant grant = resolve(List.of("R1", "R2"), Map.of(), List.of(roleScope1, roleScope2));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(1L, 2L, 3L), grant.getValues());
	}

	@Test
	@DisplayName("无 user_scope 且无 role_scope 时默认 SELF")
	void noScopeRows_shouldDefaultSelf() {
		ScopeGrant grant = resolve(List.of("R1"), Map.of(), List.of());

		assertEquals(DataScopeStorageType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("DEPT_AND_CHILD 展开闭包后仍为 DEPT_AND_CHILD")
	void deptAndChild_shouldExpandClosure() {
		RoleScopePO roleScope = roleScope("R1", "DEPT_AND_CHILD", "[10]");
		stubDescendantRows(List.of(10L, 11L, 12L));

		ScopeGrant grant = resolve(List.of("R1"), Map.of(), List.of(roleScope));

		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, grant.getScopeType());
		assertEquals(List.of(10L, 11L, 12L), grant.getValues());
	}

	@Test
	@DisplayName("scope_dept_ids 为空时 DEPT fail-close 为空列表")
	void blankScopeDeptIds_shouldMaterializeEmptyValues() {
		RoleScopePO roleScope = roleScope("R1", "DEPT", null);

		ScopeGrant grant = resolve(List.of("R1"), Map.of(), List.of(roleScope));

		assertEquals(DataScopeStorageType.DEPT, grant.getScopeType());
		assertEquals(List.of(), grant.getValues());
	}

	private ScopeGrant resolve(List<String> roleCodes, Map<Long, UserScopePO> userScopes,
			List<RoleScopePO> roleScopes) {
		return resolver.resolveEffectiveGrants(List.of(10L), Map.of(10L, roleCodes), userScopes, roleScopes).get(10L);
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
