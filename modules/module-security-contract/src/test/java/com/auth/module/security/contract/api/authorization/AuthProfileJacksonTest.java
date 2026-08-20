package com.auth.module.security.contract.api.authorization;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link AuthProfile} Jackson 往返：保证 Redis 画像反序列化保留嵌套 deptScope
 */
@DisplayName("AuthProfile Jackson")
class AuthProfileJacksonTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Test
	@DisplayName("JSON 往返保留用户字段与 deptScope")
	void readWrite_preservesProfileAndDeptScope() throws Exception {
		AuthProfile original = AuthProfile.builder()
			.userId(4L)
			.username("north_chen")
			.roles(List.of("REGION_MGR_NORTH"))
			.permissions(List.of("sys:user:query"))
			.deptScope(ScopeGrant.builder()
				.scopeType(DataScopeStorageType.DEPT_AND_CHILD)
				.values(List.of(110L, 111L, 112L))
				.build())
			.permVersion(0L)
			.build();

		String json = objectMapper.writeValueAsString(original);
		AuthProfile restored = objectMapper.readValue(json, AuthProfile.class);

		assertNotNull(restored);
		assertEquals(4L, restored.getUserId());
		assertEquals("north_chen", restored.getUsername());
		assertEquals(List.of("REGION_MGR_NORTH"), restored.getRoles());
		assertEquals(List.of("sys:user:query"), restored.getPermissions());
		assertEquals(0L, restored.getPermVersion());
		assertNotNull(restored.getDeptScope());
		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, restored.getDeptScope().getScopeType());
		assertEquals(List.of(110L, 111L, 112L), restored.getDeptScope().getValues());
	}

	@Test
	@DisplayName("Map convertValue 保留嵌套 deptScope（模拟 Redis LinkedHashMap）")
	void convertValue_fromMap_preservesDeptScope() {
		Map<String, Object> deptScope = new LinkedHashMap<>();
		deptScope.put("scopeType", "DEPT");
		deptScope.put("values", List.of(1112));

		Map<String, Object> raw = new LinkedHashMap<>();
		raw.put("userId", 11);
		raw.put("username", "bj_sales_a");
		raw.put("roles", List.of("STAFF"));
		raw.put("permissions", List.of());
		raw.put("deptScope", deptScope);
		raw.put("permVersion", 0);

		AuthProfile restored = objectMapper.convertValue(raw, AuthProfile.class);

		assertNotNull(restored);
		assertEquals(11L, restored.getUserId());
		assertEquals("bj_sales_a", restored.getUsername());
		assertNotNull(restored.getDeptScope());
		assertEquals(DataScopeStorageType.DEPT, restored.getDeptScope().getScopeType());
		assertEquals(List.of(1112L), restored.getDeptScope().getValues());
	}

}
