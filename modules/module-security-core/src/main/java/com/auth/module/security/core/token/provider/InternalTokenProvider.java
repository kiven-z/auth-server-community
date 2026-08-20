package com.auth.module.security.core.token.provider;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.TOKEN_TYPE;
import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.*;

/**
 * 内部令牌：双模签发——「用户身份」与「服务身份」共用一种令牌种类，由 principal_type claim 区分
 *
 * @author Bunny
 */
public class InternalTokenProvider extends AbstractSecurityTokenProvider {

	private final JwtProperties jwtProperties;

	public InternalTokenProvider(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
		super(jwtTokenProvider);
		this.jwtProperties = jwtProperties;
	}

	/**
	 * 应用内部令牌 claims
	 * @param builder 构建器
	 * @param claims claims
	 */
	private static void applyInternalClaims(SecurityTokenResult.SecurityTokenResultBuilder builder, Claims claims) {
		Optional.ofNullable(claims.get(PRINCIPAL_TYPE, String.class))
			.filter(CharSequenceUtil::isNotBlank)
			.ifPresent(builder::principalType);

		Optional.ofNullable(claims.get(SERVICE_ID, String.class))
			.filter(CharSequenceUtil::isNotBlank)
			.ifPresent(builder::serviceId);
	}

	/**
	 * 构建用户身份内部令牌：sub = userId，principal_type=USER
	 * @param userId 用户ID
	 * @param jti 令牌ID
	 * @return 内部令牌
	 */
	@Override
	public String buildToken(Long userId, String jti, Long permVersion) {
		Objects.requireNonNull(userId, "userId is Null");

		// 构建 JWT 构建器
		JwtBuilder builder = baseBuilder(userId, jti).claims(Map.of(PRINCIPAL_TYPE, PRINCIPAL_TYPE_USER));
		// 生成 JWT 令牌
		return super.jwtTokenProvider.generatorJwtToken(builder);
	}

	/**
	 * 构建服务身份内部令牌：sub 占位为 真实服务名写入 service_id claim，principal_type=SERVICE
	 * @param serviceId 服务名（一般为 spring.application.name）
	 * @param jti 令牌ID
	 * @return 内部令牌
	 */
	public String buildServiceToken(String serviceId, String jti) {
		// 构建 JWT 构建器
		JwtBuilder builder = baseBuilder(SERVICE_SUB_PLACEHOLDER, jti)
			.claims(Map.of(PRINCIPAL_TYPE, PRINCIPAL_TYPE_SERVICE, SERVICE_ID, serviceId));
		// 生成 JWT 令牌
		return super.jwtTokenProvider.generatorJwtToken(builder);
	}

	/**
	 * 解析后从原始 claims 补齐 {@link SecurityTokenResult} 的 principalType / serviceId
	 */
	@Override
	protected void enrichTokenResult(SecurityTokenResult.SecurityTokenResultBuilder builder, String token,
			JwtUserToken jwtUserToken, SecurityTokenKind kind) {
		// 内部令牌类型不匹配，返回
		if (kind != SecurityTokenKind.INTERNAL) {
			return;
		}
		applyInternalClaims(builder, super.jwtTokenProvider.getClaims(token));
	}

	/**
	 * 共用基础 builder：填充 issuer / subject / iat / token_type / exp
	 */
	private JwtBuilder baseBuilder(Long userId, String jti) {
		String issuer = jwtProperties.getIssuer();
		String tokenKind = getTokenKind();
		return Jwts.builder()
			.id(jti)
			.issuer(issuer)
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.claim(TOKEN_TYPE, tokenKind)
			.expiration(plusSeconds(new Date(), INTERNAL_MAX_TTL_SECONDS));
	}

	/**
	 * 获取令牌类型
	 * @return 令牌类型
	 */
	@Override
	protected String getTokenKind() {
		return SecurityTokenKind.INTERNAL.name();
	}

}
