package com.auth.common.jwt.testsupport;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.key.HmacSecretKeys;
import com.auth.common.jwt.provider.HmacJwtTokenProvider;
import lombok.experimental.UtilityClass;

import javax.crypto.SecretKey;

import static com.auth.common.jwt.model.SignatureAlgorithm.HS256;

/**
 * 单测用 JWT 配置与 Provider 构造
 */
@UtilityClass
public class JwtTestFixtures {

	private static final String HMAC_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789ab";

	/**
	 * 创建 HMAC 属性
	 * @param issuer 发行者
	 * @return 属性
	 */
	public static JwtProperties hmacProperties(String issuer) {
		JwtProperties props = new JwtProperties();
		props.setAlgorithm(HS256);
		props.setIssuer(issuer);
		props.setSecret(HMAC_SECRET);
		props.setAccessExpired(3600);
		props.setRefreshExpired(7200);
		props.setClockSkewSeconds(30);
		props.validate();
		return props;
	}

	/**
	 * 创建 HMAC Provider
	 * @param issuer 发行者
	 * @return Provider
	 */
	public static HmacJwtTokenProvider hmacProvider(String issuer) {
		JwtProperties props = hmacProperties(issuer);
		SecretKey key = HmacSecretKeys.fromUtf8Secret(props.getSecret());
		return new HmacJwtTokenProvider(props, key);
	}

	/**
	 * 创建 HMAC 密钥
	 * @return 密钥
	 */
	public static SecretKey hmacSecretKey() {
		return HmacSecretKeys.fromUtf8Secret(HMAC_SECRET);
	}

}
