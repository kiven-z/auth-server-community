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
 * {@link ScopeGrant} Jackson 往返：保证 Redis/画像反序列化不丢 scopeType
 */
@DisplayName("ScopeGrant Jackson")
class ScopeGrantJacksonTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Test
	@DisplayName("JSON 往返保留 scopeType 与 values")
	void readWrite_preservesScopeTypeAndValues() throws Exception {
		ScopeGrant original = ScopeGrant.builder()
			.scopeType(DataScopeStorageType.DEPT_AND_CHILD)
			.values(List.of(110L, 111L, 112L))
			.build();

		String json = objectMapper.writeValueAsString(original);
		ScopeGrant restored = objectMapper.readValue(json, ScopeGrant.class);

		assertNotNull(restored);
		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, restored.getScopeType());
		assertEquals(List.of(110L, 111L, 112L), restored.getValues());
	}

	@Test
	@DisplayName("Map convertValue 保留 scopeType（模拟 Redis LinkedHashMap）")
	void convertValue_fromMap_preservesScopeType() {
		Map<String, Object> raw = new LinkedHashMap<>();
		raw.put("scopeType", "DEPT");
		raw.put("values", List.of(1112));

		ScopeGrant restored = objectMapper.convertValue(raw, ScopeGrant.class);

		assertNotNull(restored);
		assertEquals(DataScopeStorageType.DEPT, restored.getScopeType());
		assertEquals(List.of(1112L), restored.getValues());
	}

}
