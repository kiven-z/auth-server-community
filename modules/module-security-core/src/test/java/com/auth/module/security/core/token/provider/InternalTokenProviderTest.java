package com.auth.module.security.core.token.provider;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.key.HmacSecretKeys;
import com.auth.common.jwt.model.SignatureAlgorithm;
import com.auth.common.jwt.provider.HmacJwtTokenProvider;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.TOKEN_TYPE;
import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.PRINCIPAL_TYPE;
import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.PRINCIPAL_TYPE_USER;
import static org.junit.jupiter.api.Assertions.*;

class InternalTokenProviderTest {

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

	private static InternalTokenProvider newProvider(JwtProperties props) {
		var key = HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
		HmacJwtTokenProvider jwt = new HmacJwtTokenProvider(props, key);
		return new InternalTokenProvider(jwt, props);
	}

	@Test
	@DisplayName("测试构建令牌时应携带令牌类型并限制在最大TTL内")
	void buildToken_shouldCarryTokenTypeAndExpireWithinMaxTtl() {
		JwtProperties props = hmacJwtProperties();
		var key = HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
		HmacJwtTokenProvider jwt = new HmacJwtTokenProvider(props, key);

		InternalTokenProvider provider = new InternalTokenProvider(jwt, props);

		Date before = new Date();
		String token = provider.buildToken(100L, "jti-1", null);
		Date after = new Date();

		var claims = jwt.getClaims(token);
		assertEquals(SecurityTokenKind.INTERNAL.name(), claims.get(TOKEN_TYPE));

		Date exp = claims.getExpiration();
		assertNotNull(exp);

		long maxMillis = SecurityInternalTokenConstants.INTERNAL_MAX_TTL_SECONDS * 1000L;
		long lowerBound = before.getTime();
		long upperBound = after.getTime() + maxMillis + 2000L; // allow small clock/exec
		// jitter
		assertTrue(exp.getTime() >= lowerBound && exp.getTime() <= upperBound);
	}

	@Test
	@DisplayName("测试构建令牌时应携带用户主体类型声明")
	void buildToken_shouldCarryUserPrincipalTypeClaim() {
		JwtProperties props = hmacJwtProperties();
		var key = HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
		HmacJwtTokenProvider jwt = new HmacJwtTokenProvider(props, key);
		InternalTokenProvider provider = new InternalTokenProvider(jwt, props);

		String token = provider.buildToken(100L, "jti-user", null);
		var claims = jwt.getClaims(token);

		assertEquals(PRINCIPAL_TYPE_USER, claims.get(PRINCIPAL_TYPE));
		assertEquals("100", claims.getSubject());
	}

	@Test
	@DisplayName("测试构建服务令牌时应携带服务主体类型和服务ID声明")
	void buildServiceToken_shouldCarryServicePrincipalTypeAndServiceIdClaim() {
		JwtProperties props = hmacJwtProperties();
		var key = HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
		HmacJwtTokenProvider jwt = new HmacJwtTokenProvider(props, key);
		InternalTokenProvider provider = new InternalTokenProvider(jwt, props);

		String token = provider.buildServiceToken("auth-server", "jti-svc");
		var claims = jwt.getClaims(token);

		assertEquals(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE, claims.get(PRINCIPAL_TYPE));
		assertEquals("auth-server", claims.get(SecurityInternalTokenConstants.SERVICE_ID));
		assertEquals(SecurityInternalTokenConstants.SERVICE_SUB_PLACEHOLDER, Long.valueOf(claims.getSubject()));
	}

	@Test
	@DisplayName("测试解析用户模式时应丰富主体类型为用户")
	void parseToken_userMode_shouldEnrichPrincipalTypeUser() {
		JwtProperties props = hmacJwtProperties();
		InternalTokenProvider provider = newProvider(props);

		String token = provider.buildToken(100L, "jti-u", null);
		SecurityTokenResult result = provider.parseToken(token);

		assertEquals(PRINCIPAL_TYPE_USER, result.getPrincipalType());
		assertEquals(100L, result.getUserToken().getUserId());
	}

	@Test
	@DisplayName("测试解析服务模式时应丰富主体类型为服务并携带服务ID")
	void parseToken_serviceMode_shouldEnrichPrincipalTypeServiceAndServiceId() {
		JwtProperties props = hmacJwtProperties();
		InternalTokenProvider provider = newProvider(props);

		String token = provider.buildServiceToken("auth-server", "jti-s");
		SecurityTokenResult result = provider.parseToken(token);

		assertEquals(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE, result.getPrincipalType());
		assertEquals("auth-server", result.getServiceId());
	}

	@Test
	@DisplayName("测试解析内部令牌时同时兼容原始 JWT 与 Bearer 前缀")
	void parseToken_shouldSupportRawAndBearerToken() {
		JwtProperties props = hmacJwtProperties();
		InternalTokenProvider provider = newProvider(props);
		String rawToken = provider.buildServiceToken("auth-server", "jti-bearer");

		SecurityTokenResult rawResult = provider.parseToken(rawToken);
		SecurityTokenResult bearerResult = provider.parseToken("Bearer " + rawToken);

		assertEquals(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE, rawResult.getPrincipalType());
		assertEquals(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE, bearerResult.getPrincipalType());
		assertEquals("auth-server", rawResult.getServiceId());
		assertEquals("auth-server", bearerResult.getServiceId());
	}

}
