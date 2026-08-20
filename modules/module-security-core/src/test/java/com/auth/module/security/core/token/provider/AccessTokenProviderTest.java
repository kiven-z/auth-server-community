package com.auth.module.security.core.token.provider;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.key.HmacSecretKeys;
import com.auth.common.jwt.provider.HmacJwtTokenProvider;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.auth.common.jwt.model.SignatureAlgorithm.HS256;
import static com.auth.module.security.core.token.support.SecurityTokenSupport.parsePermVersionClaim;
import static org.junit.jupiter.api.Assertions.*;

class AccessTokenProviderTest {

	private static final String HMAC_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab";

	private static JwtProperties hmacJwtProperties() {
		JwtProperties p = new JwtProperties();
		p.setAlgorithm(HS256);
		p.setIssuer("test-issuer");
		p.setSecret(HMAC_SECRET);
		p.setAccessExpired(3600);
		p.setRefreshExpired(7200);
		p.setClockSkewSeconds(30);
		p.validate();
		return p;
	}

	@Test
	@DisplayName("测试构建和解析访问令牌时应携带权限版本")
	void buildAndParse_accessToken_carriesPermVersion() {
		JwtProperties props = hmacJwtProperties();
		var key = HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
		HmacJwtTokenProvider jwt = new HmacJwtTokenProvider(props, key);
		AccessTokenProvider provider = new AccessTokenProvider(jwt, props);

		String token = provider.buildToken(100L, "jti-1", 42L);
		SecurityTokenResult r = provider.parseToken(token);

		assertEquals(SecurityTokenKind.EXTERNAL_ACCESS, r.getKind());
		assertEquals(42L, r.getPermVersion());
		assertEquals(100L, r.getUserToken().getUserId());
	}

	@Test
	@DisplayName("测试权限版本为空时应抛出 NullPointerException")
	void buildToken_throwsWhenPermVersionNull() {
		JwtProperties props = hmacJwtProperties();
		var key = HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
		HmacJwtTokenProvider jwt = new HmacJwtTokenProvider(props, key);
		AccessTokenProvider provider = new AccessTokenProvider(jwt, props);

		NullPointerException ex = assertThrows(NullPointerException.class,
				() -> provider.buildToken(1L, "jti-2", null));

		assertEquals("permVersion is Null", ex.getMessage());
	}

	@Test
	@DisplayName("测试解析权限版本时应处理不同类型")
	void parsePermVersionClaim_handlesTypes() {
		assertNull(parsePermVersionClaim(null));
		assertEquals(7L, parsePermVersionClaim(7));
		assertEquals(9L, parsePermVersionClaim(9L));
		assertEquals(11L, parsePermVersionClaim("11"));
		assertNull(parsePermVersionClaim(""));
		assertNull(parsePermVersionClaim("x"));
	}

}
