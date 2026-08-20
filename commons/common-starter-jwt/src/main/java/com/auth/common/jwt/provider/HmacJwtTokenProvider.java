package com.auth.common.jwt.provider;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

/**
 * HS256 实现
 *
 * @author Bunny
 */
public class HmacJwtTokenProvider extends AbstractJwtTokenProvider {

	private final SecretKey secretKey;

	public HmacJwtTokenProvider(JwtProperties properties, SecretKey secretKey) {
		super(properties);
		this.secretKey = secretKey;
	}

	/**
	 * 创建解析器
	 * @return 解析器
	 */
	@Override
	protected JwtParser createParser() {
		return Jwts.parser()
			.verifyWith(this.secretKey)
			.requireIssuer(this.properties.getIssuer())
			.clockSkewSeconds(this.properties.getClockSkewSeconds())
			.build();
	}

	/**
	 * 签名算法
	 * @param builder 构建器
	 */
	@Override
	public String generatorJwtToken(@NotNull JwtBuilder builder) {
		return builder.signWith(this.secretKey, SIG.HS256).compact();
	}

}
