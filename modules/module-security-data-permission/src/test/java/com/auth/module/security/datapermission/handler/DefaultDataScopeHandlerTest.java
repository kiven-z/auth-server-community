package com.auth.module.security.datapermission.handler;

import com.auth.module.security.contract.profile.AuthProfile;
import com.auth.module.security.contract.profile.ScopeGrant;
import com.auth.module.security.contract.profile.datascope.DataScopeDimension;
import com.auth.module.security.contract.profile.datascope.DataScopeStorageType;
import com.auth.module.security.datapermission.annotation.DataScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultDataScopeHandlerTest {

	private static DataScope defaultAnn() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedDefault");
		return method.getAnnotation(DataScope.class);
	}

	@DataScope(alias = "d")
	void annotatedDefault() {
		// fixture: annotation carrier only; never invoked
	}

	@DataScope(alias = "d", scope = DataScopeStorageType.IDS)
	void annotatedForceIds() {
		// fixture: annotation carrier only; never invoked
	}

	@DataScope(alias = "d", scope = DataScopeStorageType.SELF)
	void annotatedForceSelf() {
		// fixture: annotation carrier only; never invoked
	}

	@DataScope(alias = "d", scope = { DataScopeStorageType.IDS, DataScopeStorageType.SELF })
	void annotatedMultipleScopes() {
		// fixture: annotation carrier only; never invoked
	}

	@Test
	@DisplayName("SELF 范围使用 userColumn 并拼接别名")
	void self_shouldUseUserColumn() throws Exception {
		AuthProfile user = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(user, defaultAnn());
		assertEquals("d.created_by = 10", cond);
	}

	@Test
	@DisplayName("IDS 范围使用 dimensionColumn 生成 IN 条件")
	void dept_shouldInDeptIds() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(List.of(1L, 2L)).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.dept_id IN (1,2)", cond);
	}

	@Test
	@DisplayName("IDS_AND_CHILD 范围使用已展开 ID 列表")
	void deptAndChild_shouldInDeptIds() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder()
						.scopeType(DataScopeStorageType.IDS_AND_CHILD)
						.values(List.of(1L, 2L, 3L))
						.build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.dept_id IN (1,2,3)", cond);
	}

	@Test
	@DisplayName("受限范围 values 为空时 fail-close")
	void emptyDeptIds_shouldFailClosed() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("1 = 0", cond);
	}

	@Test
	@DisplayName("ALL 范围不追加条件")
	void all_shouldReturnNull() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertNull(cond);
	}

	@Test
	@DisplayName("超级管理员直接放行")
	void superAdmin_shouldBypass() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(1L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).values(List.of()).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertNull(cond);
	}

	@Test
	@DisplayName("dataScopes 无对应维度时默认 SELF")
	void nullDeptScope_shouldDefaultToSelf() throws Exception {
		AuthProfile profile = AuthProfile.builder().userId(10L).build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.created_by = 10", cond);
	}

	@Test
	@DisplayName("未指定 scope 时按画像范围裁决")
	void unspecifiedScope_shouldUseProfileDeptScope() throws Exception {
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(List.of(7L, 8L)).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, defaultAnn());
		assertEquals("d.dept_id IN (7,8)", cond);
	}

	@Test
	@DisplayName("注解强制 IDS 且画像无节点值时 fail-close")
	void forceDept_withoutValues_shouldFailClosed() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedForceIds");
		DataScope annotation = method.getAnnotation(DataScope.class);
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(
					Map.of(DataScopeDimension.DEPT, ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, annotation);
		assertEquals("1 = 0", cond);
	}

	@Test
	@DisplayName("注解强制 SELF 覆盖画像部门范围")
	void forceSelf_shouldOverrideProfileScopeType() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedForceSelf");
		DataScope annotation = method.getAnnotation(DataScope.class);
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder().scopeType(DataScopeStorageType.IDS).values(List.of(7L, 8L)).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, annotation);
		assertEquals("d.created_by = 10", cond);
	}

	@Test
	@DisplayName("注解写了多项 scope 时 fail-close")
	void multipleScopes_shouldFailClosed() throws Exception {
		Method method = DefaultDataScopeHandlerTest.class.getDeclaredMethod("annotatedMultipleScopes");
		DataScope annotation = method.getAnnotation(DataScope.class);
		AuthProfile profile = AuthProfile.builder()
			.userId(10L)
			.dataScopes(
					Map.of(DataScopeDimension.DEPT, ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build()))
			.build();
		String cond = new DefaultDataScopeHandler().buildCondition(profile, annotation);
		assertEquals("1 = 0", cond);
	}

}
