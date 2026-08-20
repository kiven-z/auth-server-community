package com.auth.module.security.core.token.provider;

import cn.hutool.core.util.IdUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.key.HmacSecretKeys;
import com.auth.common.jwt.model.SignatureAlgorithm;
import com.auth.common.jwt.provider.HmacJwtTokenProvider;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenProviderTest {

	private static final String HMAC_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab";

	private static JwtProperties hmacJwtProperties() {
		JwtProperties p = new JwtProperties();
		p.setAlgorithm(SignatureAlgorithm.HS256);
		p.setIssuer("test-issuer");
		p.setSecret(HMAC_SECRET);
		p.setAccessExpired(3600);
		p.setRefreshExpired(7200);
		p.setClockSkewSeconds(30);
		p.validate();
		return p;
	}

	private static RefreshTokenProvider newProvider(JwtProperties props) {
		var key = HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
		HmacJwtTokenProvider jwt = new HmacJwtTokenProvider(props, key);
		return new RefreshTokenProvider(jwt, props);
	}

	@Test
	@DisplayName("测试构建令牌时应返回非空且非空白的JWT")
	void buildToken_returnsNonBlankJwt() {
		RefreshTokenProvider provider = newProvider(hmacJwtProperties());
		String token = provider.buildToken(100L, "jti-refresh", IdUtil.getSnowflakeNextId());
		assertNotNull(token);
		assertFalse(token.isBlank());
		// JWT has 3 parts separated by dots
		assertEquals(3, token.split("\\.").length);
	}

	@Test
	@DisplayName("测试验证有效令牌时应返回true")
	void verifyToken_validToken_returnsTrue() {
		RefreshTokenProvider provider = newProvider(hmacJwtProperties());
		String token = provider.buildToken(100L, "jti-verify", IdUtil.getSnowflakeNextId());
		assertTrue(provider.verifyToken(token));
	}

	@Test
	@DisplayName("测试验证无效令牌时应返回false")
	void verifyToken_invalidToken_returnsFalse() {
		RefreshTokenProvider provider = newProvider(hmacJwtProperties());
		assertFalse(provider.verifyToken("garbage.token.here"));
		assertFalse(provider.verifyToken(""));
		assertFalse(provider.verifyToken(null));
	}

	@Test
	@DisplayName("测试解析有效令牌时应返回正确类型的结果")
	void parseToken_validToken_returnsResultWithCorrectKind() {
		RefreshTokenProvider provider = newProvider(hmacJwtProperties());
		String token = provider.buildToken(100L, "jti-parse", IdUtil.getSnowflakeNextId());
		SecurityTokenResult result = provider.parseToken(token);
		assertNotNull(result);
		assertEquals(SecurityTokenKind.EXTERNAL_REFRESH, result.getKind());
		assertEquals(100L, result.getUserToken().getUserId());
		assertEquals("jti-parse", result.getUserToken().getJti());
	}

}
