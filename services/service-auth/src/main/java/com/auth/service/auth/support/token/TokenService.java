package com.auth.service.auth.support.token;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.value.login.TokenPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * 令牌服务：访问令牌与刷新令牌的签发、过期计算与解析
 *
 * <p>
 * 本类不依赖 {@link jakarta.servlet.http.HttpServletRequest} 等 HTTP 上下文，适合在登录、 Token
 * 刷新、退出登录等不同场景下复用
 * </p>
 *
 * @author Bunny
 */
@Slf4j
@Component
public class TokenService {

	private final AccessTokenProvider accessTokenProvider;

	private final RefreshTokenProvider refreshTokenProvider;

	private final JwtProperties properties;

	public TokenService(AccessTokenProvider accessTokenProvider, RefreshTokenProvider refreshTokenProvider,
			JwtProperties properties) {
		this.accessTokenProvider = accessTokenProvider;
		this.refreshTokenProvider = refreshTokenProvider;
		this.properties = properties;
	}

	/**
	 * 签发访问令牌与刷新令牌
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 * @param permVersion 权限版本快照
	 * @return 令牌对
	 */
	public TokenPair buildTokenPair(Long userId, String jti, Long permVersion) {
		String accessToken = accessTokenProvider.buildToken(userId, jti, permVersion);
		String refreshToken = refreshTokenProvider.buildToken(userId, jti, null);
		return TokenPair.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessExpiresAt(getAccessExpiresAt())
			.build();
	}

	/**
	 * 容错解析访问令牌（永不抛出异常）
	 * @param token 访问令牌原始字符串
	 * @return 用户载荷
	 */
	public Optional<JwtUserToken> parseAccessTokenSafe(String token) {
		if (CharSequenceUtil.isBlank(token)) {
			return Optional.empty();
		}
		try {
			// 解析访问令牌
			JwtUserToken jwtUserToken = requireUserToken(accessTokenProvider.parseToken(token).getUserToken(),
					AuthResultCode.TOKEN_PARSING_FAILED);

			return Optional.of(jwtUserToken);
		}
		catch (Exception exception) {
			log.error(exception.getMessage(), exception);
			return Optional.empty();
		}
	}

	/**
	 * 解析刷新令牌
	 * @param token 刷新令牌原始字符串
	 * @return 用户载荷
	 */
	public JwtUserToken parseRefreshToken(String token) {
		try {
			JwtUserToken userToken = refreshTokenProvider.parseToken(token).getUserToken();
			return requireUserToken(userToken, AuthResultCode.REFRESH_TOKEN_MALFORMED);
		}
		catch (AuthBusinessException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new AuthBusinessException(AuthResultCode.REFRESH_TOKEN_MALFORMED);
		}
	}

	/**
	 * 获取访问令牌过期时间
	 * @return 过期时间
	 */
	public Instant getAccessExpiresAt() {
		long accessExpired = properties.getAccessExpired();
		return Instant.now().plusSeconds(accessExpired);
	}

	private JwtUserToken requireUserToken(JwtUserToken userToken, AuthResultCode code) {
		if (userToken == null || userToken.getUserId() == null || CharSequenceUtil.isBlank(userToken.getJti())) {
			throw new AuthBusinessException(code);
		}
		return userToken;
	}

}
