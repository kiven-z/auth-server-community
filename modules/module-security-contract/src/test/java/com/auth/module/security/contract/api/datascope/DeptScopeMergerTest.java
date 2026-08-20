package com.auth.module.security.contract.api.datascope;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DeptScopeMerger} 单元测试
 */
@DisplayName("DeptScopeMerger")
class DeptScopeMergerTest {

	@Test
	@DisplayName("ALL 覆盖其它类型")
	void mergeLoose_allWins() {
		ScopeGrant merged = DeptScopeMerger
			.mergeLoose(List.of(ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build(),
					ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.ALL, merged.getScopeType());
	}

	@Test
	@DisplayName("DEPT 与 DEPT_AND_CHILD 合并为 DEPT_AND_CHILD 并集")
	void mergeLoose_deptAndChildWinsType() {
		ScopeGrant merged = DeptScopeMerger.mergeLoose(List.of(
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(1L, 2L)).build(),
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(List.of(2L, 3L)).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, merged.getScopeType());
		assertEquals(List.of(1L, 2L, 3L), merged.getValues());
	}

	@Test
	@DisplayName("仅 DEPT 合并后仍为 DEPT")
	void mergeLoose_onlyDeptStaysDept() {
		ScopeGrant merged = DeptScopeMerger
			.mergeLoose(List.of(ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(2L)).build(),
					ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(2L)).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.DEPT, merged.getScopeType());
		assertEquals(List.of(2L), merged.getValues());
	}

	@Test
	@DisplayName("空集合返回 null")
	void mergeLoose_emptyReturnsNull() {
		assertNull(DeptScopeMerger.mergeLoose(List.of()));
	}

}
