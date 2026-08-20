package com.auth.common.jwt.provider;

import cn.hutool.core.util.IdUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.key.HmacSecretKeys;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.auth.common.jwt.model.SignatureAlgorithm.HS256;
import static org.junit.jupiter.api.Assertions.*;

class HmacJwtTokenProviderTest {

	private static final String ISSUER = "test-issuer";

	private static final long USER_ID = 1L;

	@Test
	@DisplayName("生成访问令牌、刷新令牌并验证")
	void mintAccessRefreshValidateAndRemaining() {
		JwtProperties props = new JwtProperties();
		props.setAlgorithm(HS256);
		props.setIssuer(ISSUER);
		props.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab");
		props.setAccessExpired(3600);
		props.setRefreshExpired(7200);
		props.validate();
		HmacJwtTokenProvider provider = new HmacJwtTokenProvider(props,
				HmacSecretKeys.fromUtf8Secret(props.getSecret()));

		// 访问令牌
		JwtBuilder accessTokenJwtBuilder = Jwts.builder()
			.id(IdUtil.fastSimpleUUID())
			.issuer(ISSUER)
			.subject(String.valueOf(USER_ID))
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + 3600 * 1000));
		String access = provider.generatorJwtToken(accessTokenJwtBuilder);
		assertTrue(provider.validateToken(access));
		assertEquals(USER_ID, provider.parseToken(access).getUserId());
		long remaining = provider.getRemainingSeconds(access);
		assertTrue(remaining > 0);

		// 刷新令牌
		JwtBuilder refreshTokenJwtBuilder = Jwts.builder()
			.id(IdUtil.fastSimpleUUID())
			.issuer(ISSUER)
			.subject(String.valueOf(USER_ID))
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + 3600 * 7000));
		String refresh = provider.generatorJwtToken(refreshTokenJwtBuilder);
		assertTrue(provider.validateToken(refresh));
		assertFalse(provider.validateToken(""));
		assertFalse(provider.validateToken(null));
	}

}
