package com.auth.module.security.contract.profile.datascope;

import com.auth.module.security.contract.profile.ScopeGrant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ScopeGrantMerger} 单元测试
 */
@DisplayName("ScopeGrantMerger")
class ScopeGrantMergerTest {

	@Test
	@DisplayName("ALL 覆盖其它类型")
	void mergeLoose_allWins() {
		ScopeGrant merged = ScopeGrantMerger
			.mergeLoose(List.of(ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build(),
					ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.ALL, merged.getScopeType());
	}

	@Test
	@DisplayName("IDS 与 IDS_AND_CHILD 合并为 IDS_AND_CHILD 并集")
	void mergeLoose_idsAndChildWinsType() {
		ScopeGrant merged = ScopeGrantMerger.mergeLoose(List.of(
				ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(List.of(1L, 2L)).build(),
				ScopeGrant.builder().scopeType(DataScopeStorageType.IDS_AND_CHILD).values(List.of(2L, 3L)).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.IDS_AND_CHILD, merged.getScopeType());
		assertEquals(List.of(1L, 2L, 3L), merged.getValues());
	}

	@Test
	@DisplayName("仅 IDS 合并后仍为 IDS")
	void mergeLoose_onlyIdsStaysIds() {
		ScopeGrant merged = ScopeGrantMerger
			.mergeLoose(List.of(ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(List.of(2L)).build(),
					ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(List.of(2L)).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.IDS, merged.getScopeType());
		assertEquals(List.of(2L), merged.getValues());
	}

	@Test
	@DisplayName("空集合返回 null")
	void mergeLoose_emptyReturnsNull() {
		assertNull(ScopeGrantMerger.mergeLoose(List.of()));
	}

}
