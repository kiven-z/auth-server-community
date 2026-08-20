package com.auth.common.jwt.provider;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.exception.InvalidTokenException;
import com.auth.common.jwt.exception.JwtExpiredException;
import com.auth.common.jwt.testsupport.JwtTestFixtures;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class HmacJwtTokenProviderForgeryTest {

	@Test
	@DisplayName("拒绝错误发行者")
	void wrongIssuerRejectedOnParse() {
		JwtProperties props = JwtTestFixtures.hmacProperties("expected-iss");
		SecretKey key = JwtTestFixtures.hmacSecretKey();
		String rogue = Jwts.builder()
			.issuer("other-issuer")
			.subject("1")
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + 60_000L))
			.id("jti-wrong-iss")
			.signWith(key, Jwts.SIG.HS256)
			.compact();
		HmacJwtTokenProvider provider = new HmacJwtTokenProvider(props, key);
		assertThrows(InvalidTokenException.class, () -> provider.parseToken(rogue));
	}

	@Test
	@DisplayName("过期令牌验证失败")
	void expiredTokenFailsValidationAndParse() {
		HmacJwtTokenProvider provider = JwtTestFixtures.hmacProvider("exp-issuer");
		SecretKey key = JwtTestFixtures.hmacSecretKey();
		String expired = Jwts.builder()
			.issuer("exp-issuer")
			.subject("1")
			.issuedAt(new Date(System.currentTimeMillis() - 120_000L))
			.expiration(new Date(System.currentTimeMillis() - 60_000L))
			.id("jti-expired")
			.signWith(key, Jwts.SIG.HS256)
			.compact();
		assertFalse(provider.validateToken(expired));
		assertThrows(JwtExpiredException.class, () -> provider.parseToken(expired));
	}

	@Test
	@DisplayName("篡改负载验证失败")
	void tamperedPayloadFailsValidation() {
		HmacJwtTokenProvider provider = JwtTestFixtures.hmacProvider("tamper-issuer");
		String valid = provider.generatorJwtToken(Jwts.builder().subject("3"));
		String[] parts = valid.split("\\.");
		assertTrue(parts.length >= 3);
		String payload = parts[1];
		char[] chars = payload.toCharArray();
		chars[0] = chars[0] == 'A' ? 'B' : 'A';
		String tampered = parts[0] + "." + new String(chars) + "." + parts[2];
		assertFalse(provider.validateToken(tampered));
	}

}
