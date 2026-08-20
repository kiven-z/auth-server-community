package com.auth.common.jwt.key;

import com.auth.common.jwt.exception.JwtKeyLoadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HmacSecretKeysTest {

	@Test
	@DisplayName("拒绝空密钥")
	void rejectsNullSecret() {
		assertThrows(JwtKeyLoadException.class, () -> HmacSecretKeys.fromUtf8Secret(null));
		assertThrows(JwtKeyLoadException.class, () -> HmacSecretKeys.fromUtf8Secret("   "));

		String shortSecret = "012345678901234567890123456789"; // 30 chars
		assertThrows(JwtKeyLoadException.class, () -> HmacSecretKeys.fromUtf8Secret(shortSecret));

		String secret = "0123456789abcdef0123456789abcdef"; // 32 ASCII bytes
		assertNotNull(HmacSecretKeys.fromUtf8Secret(secret));
	}

}
