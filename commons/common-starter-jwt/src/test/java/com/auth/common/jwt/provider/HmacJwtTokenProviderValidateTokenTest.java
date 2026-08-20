package com.auth.common.jwt.provider;

import com.auth.common.jwt.testsupport.JwtTestFixtures;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacJwtTokenProviderValidateTokenTest {

	private static final String ISSUER = "validate-issuer";

	private HmacJwtTokenProvider provider;

	private String validToken;

	@BeforeEach
	void setUp() {
		this.provider = JwtTestFixtures.hmacProvider(ISSUER);
		this.validToken = this.provider.generatorJwtToken(Jwts.builder().issuer(ISSUER).subject("3"));
	}

	@Test
	@DisplayName("验证令牌为空、空白或空格时返回 false")
	void validateTokenFalseForNullBlankOrWhitespace() {
		assertFalse(this.provider.validateToken(null));
		assertFalse(this.provider.validateToken(""));
		assertFalse(this.provider.validateToken("   "));
		assertFalse(this.provider.validateToken("Bearer "));
		assertFalse(this.provider.validateToken("Bearer   "));
		assertTrue(this.provider.validateToken(this.validToken));
		assertTrue(this.provider.validateToken("Bearer " + this.validToken));
		assertTrue(this.provider.validateToken("bearer " + this.validToken));
	}

}
