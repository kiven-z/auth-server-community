package com.auth.common.jwt.provider;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import org.jetbrains.annotations.NotNull;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RS256 实现
 *
 * @author Bunny
 */
public class RsaJwtTokenProvider extends AbstractJwtTokenProvider {

	private final PrivateKey privateKey;

	private final PublicKey publicKey;

	public RsaJwtTokenProvider(JwtProperties properties, PrivateKey privateKey, PublicKey publicKey) {
		super(properties);
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	/**
	 * 创建 Parser
	 * @return Parser
	 */
	@Override
	protected JwtParser createParser() {
		return Jwts.parser()
			.verifyWith(this.publicKey)
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
		return builder.signWith(this.privateKey, SIG.RS256).compact();
	}

}
