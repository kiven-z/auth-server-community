package com.auth.module.security.core.token.provider;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.exception.JwtExpiredException;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import io.jsonwebtoken.Claims;

import java.util.Date;

import static com.auth.common.jwt.provider.AbstractJwtTokenProvider.BEARER_PREFIX;
import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.TOKEN_TYPE;

/**
 * 抽象令牌提供者
 *
 * @author Bunny
 */
public abstract class AbstractSecurityTokenProvider implements SecurityTokenProvider {

	protected final JwtTokenProvider jwtTokenProvider;

	protected AbstractSecurityTokenProvider(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/**
	 * 添加秒数
	 * @param base 基础时间
	 * @param seconds 秒数
	 * @return 时间
	 */
	protected Date plusSeconds(Date base, long seconds) {
		return new Date(base.getTime() + seconds * 1000L);
	}

	/**
	 * 验证令牌是否合法、是否过期、是否是对应的类型
	 * @param token 令牌
	 * @return 是否有效
	 */
	@Override
	public boolean verifyToken(String token) {
		try {
			parseToken(token);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	/**
	 * 获取令牌类型
	 * @return 令牌类型
	 */
	protected abstract String getTokenKind();

	/**
	 * 解析令牌
	 * @param token 令牌
	 * @return 解析结果
	 */
	@Override
	public SecurityTokenResult parseToken(String token) {
		// 格式化令牌，仅在开头为 Bearer 且不能为空
		String normalizedToken = normalizeToken(token);
		if (CharSequenceUtil.isBlank(normalizedToken)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_MISSING, "Token is missing.");
		}

		try {
			// 获取令牌声明
			Claims claims = jwtTokenProvider.getClaims(normalizedToken);
			// 获取令牌类型
			Object raw = claims.get(TOKEN_TYPE);
			// 获取令牌类型
			String tokenKind = getTokenKind();

			// 如果令牌类型不匹配
			if (raw == null || !CharSequenceUtil.equals(raw.toString(), tokenKind, true)) {
				throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_KIND_MISMATCH, "Token kind mismatch.");
			}

			// 解析令牌
			JwtUserToken jwtUserToken = jwtTokenProvider.parseToken(normalizedToken);

			// 获取令牌类型
			SecurityTokenKind kind = SecurityTokenKind.of(tokenKind);
			// 如果令牌类型不存在
			if (kind == null) {
				throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_KIND_MISMATCH,
						"Token kind is not supported: " + tokenKind);
			}

			// 构建令牌结果
			SecurityTokenResult.SecurityTokenResultBuilder builder = SecurityTokenResult.builder()
				.kind(kind)
				.rawToken(normalizedToken)
				.userToken(jwtUserToken);
			enrichTokenResult(builder, normalizedToken, jwtUserToken, kind);
			return builder.build();
		}
		catch (SecurityTokenException ex) {
			throw ex;
		}
		catch (JwtExpiredException ex) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_EXPIRED, "Token has expired.");
		}
		catch (Exception ex) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "Token is invalid.");
		}
	}

	/**
	 * 格式化令牌：仅在 Bearer 前缀存在时裁剪前缀
	 * @param token 令牌
	 * @return 格式化后的令牌
	 */
	private String normalizeToken(String token) {
		String trimmedToken = CharSequenceUtil.trim(token);
		if (CharSequenceUtil.isBlank(trimmedToken)) {
			return trimmedToken;
		}

		return CharSequenceUtil.startWithIgnoreCase(trimmedToken, BEARER_PREFIX)
				? CharSequenceUtil.trim(CharSequenceUtil.removePrefixIgnoreCase(trimmedToken, BEARER_PREFIX))
				: trimmedToken;
	}

	/**
	 * 子类在解析完成后补充 {@link SecurityTokenResult} 字段（例如外部 Access Token 的 perm_version）
	 * @param builder 构建器
	 * @param token 令牌
	 * @param jwtUserToken JWT用户令牌
	 * @param kind 令牌类型
	 */
	protected abstract void enrichTokenResult(SecurityTokenResult.SecurityTokenResultBuilder builder, String token,
			JwtUserToken jwtUserToken, SecurityTokenKind kind);

}
