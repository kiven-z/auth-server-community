package com.auth.service.system.authorization.outbox;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidatePayload;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * {@link InvalidationOutboxPayloadCodec} 单元测试
 */
@DisplayName("InvalidationOutboxPayloadCodec Outbox Payload 编解码")
class InvalidationOutboxPayloadCodecTest {

	private final InvalidationOutboxPayloadCodec codec = new InvalidationOutboxPayloadCodec(new ObjectMapper());

	@Test
	@DisplayName("ROLE Payload 序列化后可按 kind 反序列化")
	void roundTrip_rolePayload() {
		RoleInvalidatePayload original = new RoleInvalidatePayload(List.of("SUPER_ADMIN"));
		String json = codec.serialize(original);

		AuthorizationInvalidatePayload restored = codec.deserialize(AuthorizationChangeKind.ROLE, json);

		assertInstanceOf(RoleInvalidatePayload.class, restored);
		assertEquals(original.roleCodes(), ((RoleInvalidatePayload) restored).roleCodes());
	}

}
