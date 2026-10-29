package com.auth.module.security.contract.profile;

import com.auth.module.security.contract.profile.datascope.DataScopeDimension;
import com.auth.module.security.contract.profile.datascope.DataScopeStorageType;
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
 * {@link AuthProfile} Jackson 往返：保证 Redis 画像反序列化保留 dataScopes
 */
@DisplayName("AuthProfile Jackson")
class AuthProfileJacksonTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Test
	@DisplayName("JSON 往返保留 dataScopes Map 与嵌套 ScopeGrant")
	void readWrite_preservesDataScopes() throws Exception {
		AuthProfile original = AuthProfile.builder()
			.userId(4L)
			.username("north_chen")
			.roles(List.of("REGION_MGR_NORTH"))
			.permissions(List.of("sys:user:query"))
			.dataScopes(Map.of(DataScopeDimension.DEPT,
					ScopeGrant.builder()
						.scopeType(DataScopeStorageType.IDS_AND_CHILD)
						.values(List.of(110L, 111L, 112L))
						.build()))
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
		ScopeGrant deptGrant = restored.getDataScopes().get(DataScopeDimension.DEPT);
		assertNotNull(deptGrant);
		assertEquals(DataScopeStorageType.IDS_AND_CHILD, deptGrant.getScopeType());
		assertEquals(List.of(110L, 111L, 112L), deptGrant.getValues());
	}

	@Test
	@DisplayName("Map convertValue 保留 dataScopes（模拟 Redis LinkedHashMap）")
	void convertValue_fromMap_preservesDataScopes() {
		Map<String, Object> scopeGrant = new LinkedHashMap<>();
		scopeGrant.put("scopeType", "IDS");
		scopeGrant.put("values", List.of(1112));

		Map<String, Object> raw = new LinkedHashMap<>();
		raw.put("userId", 11);
		raw.put("username", "bj_sales_a");
		raw.put("roles", List.of("STAFF"));
		raw.put("permissions", List.of());
		raw.put("dataScopes", Map.of("DEPT", scopeGrant));
		raw.put("permVersion", 0);

		AuthProfile restored = objectMapper.convertValue(raw, AuthProfile.class);

		assertNotNull(restored);
		assertEquals(11L, restored.getUserId());
		assertEquals("bj_sales_a", restored.getUsername());
		ScopeGrant deptGrant = restored.getDataScopes().get(DataScopeDimension.DEPT);
		assertNotNull(deptGrant);
		assertEquals(DataScopeStorageType.IDS, deptGrant.getScopeType());
		assertEquals(List.of(1112L), deptGrant.getValues());
	}

}
