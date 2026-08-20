package com.auth.module.security.core.token.provider;

import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.model.SecurityTokenResult.SecurityTokenResultBuilder;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.TOKEN_TYPE;

/**
 * 刷新令牌
 *
 * @author Bunny
 */
public class RefreshTokenProvider extends AbstractSecurityTokenProvider {

	private final JwtProperties properties;

	public RefreshTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties properties) {
		super(jwtTokenProvider);
		this.properties = properties;
	}

	/**
	 * 构建刷新令牌
	 * @param userId 用户ID
	 * @param jti 令牌ID
	 * @param permVersion 可以为空，刷新令牌不记录
	 * @return 刷新令牌
	 */
	@Override
	public String buildToken(Long userId, String jti, Long permVersion) {
		Objects.requireNonNull(userId, "userId is Null");

		// 签发者：指明该 JWT 的签发方（例如身份认证服务或应用标识）
		String issuer = properties.getIssuer();

		// 刷新令牌访问时间
		long refreshExpired = properties.getRefreshExpired();

		// 令牌类型
		JwtBuilder jwtBuilder = Jwts.builder()
			.id(jti)
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.claims(Map.of(
					// Token类型
					TOKEN_TYPE, getTokenKind()))
			.expiration(plusSeconds(new Date(), refreshExpired));

		return super.jwtTokenProvider.generatorJwtToken(jwtBuilder);
	}

	/**
	 * 获取令牌类型
	 * @return 令牌类型
	 */
	@Override
	protected String getTokenKind() {
		return SecurityTokenKind.EXTERNAL_REFRESH.name();
	}

	/**
	 * 子类在解析完成后补充 {@link SecurityTokenResult} 字段（例如外部 Access Token 的 perm_version）
	 */
	@Override
	protected void enrichTokenResult(SecurityTokenResultBuilder builder, String token, JwtUserToken jwtUserToken,
			SecurityTokenKind kind) {
		// Refresh Token does not carry perm_version
	}

}
