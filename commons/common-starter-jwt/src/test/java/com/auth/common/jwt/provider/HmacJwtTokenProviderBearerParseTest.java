package com.auth.common.jwt.provider;

import com.auth.common.jwt.exception.InvalidTokenException;
import com.auth.common.jwt.testsupport.JwtTestFixtures;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class HmacJwtTokenProviderBearerParseTest {

	@Test
	@DisplayName("parseToken/getClaims/getRemainingSeconds 支持 Authorization: Bearer <token> 输入")
	void bearerInputWorksForParseClaimsAndRemaining() {
		HmacJwtTokenProvider provider = JwtTestFixtures.hmacProvider("bearer-issuer");
		String token = provider.generatorJwtToken(Jwts.builder()
			.issuer("bearer-issuer")
			.subject("9")
			.expiration(new Date(System.currentTimeMillis() + 60_000L)));
		String bearer = "Bearer " + token;

		assertEquals(9L, provider.parseToken(bearer).getUserId());
		assertEquals("9", provider.getClaims(bearer).getSubject());
		assertTrue(provider.getRemainingSeconds(bearer) > 0);
	}

	@Test
	@DisplayName("sub 非数字时抛出 InvalidTokenException")
	void subjectMustBeNumericUserId() {
		HmacJwtTokenProvider provider = JwtTestFixtures.hmacProvider("sub-issuer");
		String token = provider.generatorJwtToken(Jwts.builder().issuer("sub-issuer").subject("user-abc"));
		assertThrows(InvalidTokenException.class, () -> provider.parseToken(token));
	}

}
