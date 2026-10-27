package com.auth.module.security.contract.invalidation;

import com.auth.module.security.contract.grant.GrantTableSubjectType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AuthorizationInvalidatePayload} 单元测试
 *
 * @author Bunny
 */
@DisplayName("AuthorizationInvalidatePayload 失效负载校验")
class AuthorizationInvalidatePayloadTest {

	@Test
	@DisplayName("列表应拷贝为不可变列表")
	void role_shouldCopyToUnmodifiableList() {
		List<String> source = new ArrayList<>(List.of("ADMIN"));

		AuthorizationInvalidatePayload.Role payload = new AuthorizationInvalidatePayload.Role(source);
		source.add("OPERATOR");

		assertEquals(List.of("ADMIN"), payload.roleCodes());
		assertThrows(UnsupportedOperationException.class, () -> payload.roleCodes().add("X"));
	}

	@Test
	@DisplayName("空列表或 null 应拒绝")
	void role_whenNullOrEmpty_shouldThrow() {
		List<String> nullSource = null;
		assertThrows(IllegalArgumentException.class, () -> new AuthorizationInvalidatePayload.Role(nullSource));
		assertThrows(IllegalArgumentException.class,
				() -> new AuthorizationInvalidatePayload.Role(Collections.emptyList()));
	}

	@Test
	@DisplayName("主体键字段为 null 时应拒绝")
	void grantSubjectKey_whenNull_shouldThrow() {
		assertThrows(IllegalArgumentException.class, () -> new GrantSubjectKey(null, 1L));
		assertThrows(IllegalArgumentException.class, () -> new GrantSubjectKey(GrantTableSubjectType.USER, null));
	}

}
