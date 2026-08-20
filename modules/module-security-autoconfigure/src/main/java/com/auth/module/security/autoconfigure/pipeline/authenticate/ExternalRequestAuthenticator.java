package com.auth.module.security.autoconfigure.pipeline.authenticate;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.autoconfigure.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.util.Objects;

/**
 * 外部 Bearer Token 认证：校验令牌、强会话、权限版本与并发会话
 *
 * @author Bunny
 */
public final class ExternalRequestAuthenticator implements RequestAuthenticator {

	private final AccessTokenProvider accessTokenProvider;

	/**
	 * 授权画像缓存服务
	 */
	private final AuthProfileCacheService authProfileCacheService;

	/**
	 * 会话计数检查器
	 */
	private final SessionCountChecker sessionCountChecker;

	/**
	 * 构造函数
	 */
	public ExternalRequestAuthenticator(AccessTokenProvider accessTokenProvider,
			AuthProfileCacheService authProfileCacheService, SessionCountChecker sessionCountChecker) {
		this.accessTokenProvider = accessTokenProvider;
		this.authProfileCacheService = authProfileCacheService;
		this.sessionCountChecker = sessionCountChecker;
	}

	/**
	 * 支持请求
	 * @param request HTTP 请求
	 * @return 是否支持
	 */
	@Override
	public boolean supports(HttpServletRequest request) {
		String internalHeader = request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER);
		if (CharSequenceUtil.isNotBlank(internalHeader)) {
			return false;
		}

		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		return CharSequenceUtil.isNotBlank(authorization);
	}

	/**
	 * 认证请求
	 * @param request HTTP 请求
	 * @return 授权画像
	 */
	@Override
	public AuthProfile authenticate(HttpServletRequest request) {
		// 解析请求头内容，解析当前用户传递的Token
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		SecurityTokenResult result = accessTokenProvider.parseToken(authorization);
		JwtUserToken userToken = result.getUserToken();
		Long userId = userToken.getUserId();

		String sessionId = userToken.getJti();
		request.setAttribute(SecurityRequestAttributes.SESSION_ID, sessionId);
		if (userId == null) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "User id is missing in token.");
		}
		sessionCountChecker.verifySessionPresent(sessionId);

		// 用户信息也需要存在
		AuthProfile profile = authProfileCacheService.load(userId);
		if (profile == null) {
			throw new SecurityTokenException(SecurityResultCodeEnum.SESSION_MISSING, "Auth profile is missing.");
		}

		// 比对版本信息
		assertPermVersionConsistent(result.getPermVersion(), profile);
		sessionCountChecker.verifyConcurrentSessionsWithinLimit(userId, sessionId);
		return profile;
	}

	/**
	 * 断言权限版本一致
	 * @param snapshot 权限版本快照
	 * @param profile 授权画像
	 */
	private void assertPermVersionConsistent(Long snapshot, AuthProfile profile) {
		Long current = profile.getPermVersion();
		if (Objects.equals(snapshot, current)) {
			return;
		}
		throw new SecurityTokenException(SecurityResultCodeEnum.PERMISSION_VERSION_MISMATCH,
				"Permission version mismatch.");
	}

}
