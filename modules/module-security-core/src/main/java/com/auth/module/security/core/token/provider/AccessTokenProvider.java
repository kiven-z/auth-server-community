package com.auth.module.security.core.token.provider;

import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.PERM_VERSION;
import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.TOKEN_TYPE;
import static com.auth.module.security.core.token.support.SecurityTokenSupport.parsePermVersionClaim;

/**
 * 访问令牌支持
 *
 * @author Bunny
 */
public class AccessTokenProvider extends AbstractSecurityTokenProvider {

	private final JwtProperties properties;

	public AccessTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties properties) {
		super(jwtTokenProvider);
		this.properties = properties;
	}

	/**
	 * 构建访问令牌（不携带权限版本快照）
	 * @param userId 用户 ID
	 * @param jti 令牌 ID（会话绑定）
	 * @return 访问令牌
	 */
	@Override
	public String buildToken(Long userId, String jti, Long permVersion) {
		Objects.requireNonNull(userId, "userId is Null");
		Objects.requireNonNull(permVersion, "permVersion is Null");

		// 获取发行者
		String issuer = properties.getIssuer();
		// 获取访问过期时间
		long accessExpired = properties.getAccessExpired();

		// 构建 JWT 构建器
		JwtBuilder jwtBuilder = Jwts.builder()
			.id(jti)
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.claims(Map.of(
					// Token类型
					TOKEN_TYPE, getTokenKind(),
					// 版本号
					PERM_VERSION, permVersion))
			.expiration(plusSeconds(new Date(), accessExpired));
		return super.jwtTokenProvider.generatorJwtToken(jwtBuilder);
	}

	/**
	 * 填充令牌结果
	 * @param builder 令牌结果构建器
	 * @param token 令牌
	 * @param jwtUserToken 令牌用户
	 * @param kind 令牌类型
	 */
	@Override
	protected void enrichTokenResult(SecurityTokenResult.SecurityTokenResultBuilder builder, String token,
			JwtUserToken jwtUserToken, SecurityTokenKind kind) {
		// 访问令牌类型不匹配，返回
		if (kind != SecurityTokenKind.EXTERNAL_ACCESS) {
			return;
		}

		builder.permVersion(parsePermVersionClaim(jwtTokenProvider.getClaims(token).get(PERM_VERSION)));
	}

	/**
	 * 获取令牌类型
	 * @return 令牌类型
	 */
	@Override
	protected String getTokenKind() {
		return SecurityTokenKind.EXTERNAL_ACCESS.name();
	}

}
