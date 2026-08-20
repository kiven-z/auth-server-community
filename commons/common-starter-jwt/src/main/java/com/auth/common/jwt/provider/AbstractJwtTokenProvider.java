package com.auth.common.jwt.provider;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.exception.InvalidTokenException;
import com.auth.common.jwt.exception.JwtExpiredException;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.common.jwt.support.JwtAudienceCodec;
import com.auth.common.jwt.support.JwtExceptionTranslator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * JJWT 0.12 模板：子类绑定算法与密钥材料
 *
 * @author Bunny
 */
public abstract class AbstractJwtTokenProvider implements JwtTokenProvider {

	public static final String BEARER_PREFIX = "Bearer ";

	protected final JwtProperties properties;

	protected AbstractJwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
	}

	/**
	 * 规范化 token：trim + 去掉 Bearer 前缀（忽略大小写）
	 * <p>
	 * 无法得到有效 token 时抛出 {@link IllegalArgumentException}，由调用方决定吞掉或翻译
	 * </p>
	 * @param token 令牌
	 * @return 规范化后的原始 JWT（不含 Bearer 前缀）
	 */
	private static String normalizeToken(String token) {
		String trimmed = CharSequenceUtil.trim(token);
		Assert.hasText(trimmed, "Token is Empty");

		String withoutBearer = CharSequenceUtil.removePrefixIgnoreCase(trimmed, BEARER_PREFIX);
		withoutBearer = CharSequenceUtil.trim(withoutBearer);
		Assert.hasText(withoutBearer, "Token is Empty");
		return withoutBearer;
	}

	private static long requireNumericUserIdSubject(String subject) {
		if (CharSequenceUtil.isBlank(subject)) {
			throw new InvalidTokenException("JWT is missing sub claim.");
		}
		try {
			return Long.parseLong(subject);
		}
		catch (NumberFormatException nfe) {
			throw new InvalidTokenException("JWT sub must be a numeric userId but was: " + subject, nfe);
		}
	}

	/**
	 * 创建解析器
	 * @return JWT解析
	 */
	protected abstract JwtParser createParser();

	/**
	 * 验证 Token
	 * @param token Token
	 * @return 是否有效
	 */
	@Override
	public boolean validateToken(String token) {
		try {
			String normalized = normalizeToken(token);
			createParser().parseSignedClaims(normalized);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	@Override
	public JwtUserToken parseToken(String token) {
		Claims claims = getClaims(token);
		try {
			String subject = claims.getSubject();
			long userId = requireNumericUserIdSubject(subject);
			return JwtUserToken.builder()
				.userId(userId)
				.iss(claims.getIssuer())
				.sub(subject)
				.jti(claims.getId())
				.audience(JwtAudienceCodec.first(claims))
				.build();
		}
		catch (Exception ex) {
			throw JwtExceptionTranslator.translate(ex);
		}
	}

	/**
	 * 获取 Claims
	 * @param token Token
	 * @return Claims
	 */
	@Override
	public Claims getClaims(String token) {
		try {
			String normalized = normalizeToken(token);
			return createParser().parseSignedClaims(normalized).getPayload();
		}
		catch (Exception ex) {
			throw JwtExceptionTranslator.translate(ex);
		}
	}

	/**
	 * 获取剩余秒数
	 * @param token Token
	 * @return 剩余秒数
	 */
	@Override
	public long getRemainingSeconds(String token) {
		Claims claims = getClaims(token);
		Date exp = claims.getExpiration();
		if (exp == null) {
			throw new InvalidTokenException("JWT is missing exp claim.");
		}
		long remainingMs = exp.getTime() - System.currentTimeMillis();
		if (remainingMs <= 0) {
			throw new JwtExpiredException("JWT has expired.");
		}
		return remainingMs / 1000L;
	}

}
