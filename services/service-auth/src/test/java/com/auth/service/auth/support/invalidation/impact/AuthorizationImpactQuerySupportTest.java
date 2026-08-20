package com.auth.service.auth.support.invalidation.impact;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationImpactQuerySupport} 单元测试
 */
@DisplayName("AuthorizationImpactQuerySupport 影响面查询")
@ExtendWith(MockitoExtension.class)
class AuthorizationImpactQuerySupportTest {

	@Mock
	private AuthorizationImpactMapper authorizationImpactMapper;

	private AuthorizationImpactQuerySupport impactQuery;

	@BeforeEach
	void setUp() {
		impactQuery = new AuthorizationImpactQuerySupport(authorizationImpactMapper);
	}

	@Test
	@DisplayName("空角色码列表不访问数据库")
	void findUserIdsByRoleCodes_emptyInput_shouldNotCallMapper() {

		assertTrue(impactQuery.findUserIdsByRoleCodes(List.of()).isEmpty());
		verifyNoInteractions(authorizationImpactMapper);
	}

	@Test
	@DisplayName("入参去重后查询 Mapper，SQL 层已 DISTINCT 返回结果")
	void findUserIdsByRoleCodes_shouldDistinctBeforeQuery() {
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(1L, 2L));

		Set<Long> userIds = impactQuery.findUserIdsByRoleCodes(List.of("R_A", "R_A"));

		assertEquals(Set.of(1L, 2L), userIds);
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

	@Test
	@DisplayName("按 grant USER 主体反查用户")
	void findUserIdsByGrantSubjects_shouldResolveUserSubjectsOnly() {
		when(authorizationImpactMapper.selectUserIdsByGrantUserSubjectIds(List.of(10L))).thenReturn(List.of(10L));

		Set<Long> userIds = impactQuery
			.findUserIdsByGrantSubjects(List.of(new GrantSubjectKey(GrantTableSubjectType.USER, 10L)));

		assertEquals(Set.of(10L), userIds);
		verify(authorizationImpactMapper).selectUserIdsByGrantUserSubjectIds(List.of(10L));
	}

	@Test
	@DisplayName("权限码先桥接角色码再反查用户")
	void findUserIdsByPermissionCodes_shouldBridgeToRoleCodes() {
		when(authorizationImpactMapper.selectRoleCodesByPermissionCodes(List.of("P_A"))).thenReturn(List.of("R_A"));
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R_A"))).thenReturn(List.of(5L));

		Set<Long> userIds = impactQuery.findUserIdsByPermissionCodes(List.of("P_A"));

		assertEquals(Set.of(5L), userIds);
		verify(authorizationImpactMapper).selectRoleCodesByPermissionCodes(List.of("P_A"));
		verify(authorizationImpactMapper).selectUserIdsByRoleCodes(List.of("R_A"));
	}

	@Test
	@DisplayName("部门 ID 超过分片大小时应分批查询并合并去重")
	void findUserIdsByDeptIds_largeInput_shouldQueryInChunks() {
		List<Long> deptIds = IntStream.rangeClosed(1, BatchSizes.SIZE_500 * 2 + 200).mapToObj(Long::valueOf).toList();

		when(authorizationImpactMapper.selectUserIdsByDeptIds(anyList())).thenAnswer(invocation -> {
			List<Long> chunk = invocation.getArgument(0);
			return List.of(chunk.get(0));
		});

		Set<Long> userIds = impactQuery.findUserIdsByLongKeys(deptIds,
				authorizationImpactMapper::selectUserIdsByDeptIds);

		assertEquals(3, userIds.size());
		verify(authorizationImpactMapper, times(3)).selectUserIdsByDeptIds(anyList());
	}

	@Test
	@DisplayName("角色码超过分片大小时应分批查询")
	void findUserIdsByRoleCodes_largeInput_shouldQueryInChunks() {
		List<String> roleCodes = IntStream.rangeClosed(1, BatchSizes.SIZE_500 * 2 + 1).mapToObj(i -> "R_" + i).toList();

		when(authorizationImpactMapper.selectUserIdsByRoleCodes(anyList())).thenAnswer(invocation -> {
			List<String> chunk = invocation.getArgument(0);
			return List.of((long) chunk.size());
		});

		Set<Long> userIds = impactQuery.findUserIdsByRoleCodes(roleCodes);

		assertEquals(Set.of((long) BatchSizes.SIZE_500, 1L), userIds);
		verify(authorizationImpactMapper, times(3)).selectUserIdsByRoleCodes(anyList());
	}

}
