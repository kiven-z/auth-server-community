package com.auth.module.security.autoconfigure.pipeline.authenticate;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.autoconfigure.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 内部JWT认证：支持用户身份和服务身份两种内部令牌
 *
 * <p>
 * 用户身份令牌（principal_type=USER）：从缓存重建用户画像，支持数据权限等功能 <br>
 * 服务身份令牌（principal_type=SERVICE）：构建服务身份画像，用于无用户上下文场景
 *
 * @author Bunny
 */
public final class InternalRequestAuthenticator implements RequestAuthenticator {

	private final InternalTokenProvider internalTokenProvider;

	private final AuthProfileCacheService authProfileCacheService;

	public InternalRequestAuthenticator(InternalTokenProvider internalTokenProvider,
			AuthProfileCacheService authProfileCacheService) {
		this.internalTokenProvider = internalTokenProvider;
		this.authProfileCacheService = authProfileCacheService;
	}

	/**
	 * 支持请求
	 * @param request HTTP 请求
	 * @return 是否支持
	 */
	@Override
	public boolean supports(HttpServletRequest request) {
		return CharSequenceUtil.isNotBlank(request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER));
	}

	/**
	 * 认证请求：根据令牌类型（用户身份/服务身份）返回对应的 AuthProfile
	 * @param request HTTP 请求
	 * @return 授权画像
	 */
	@Override
	public AuthProfile authenticate(HttpServletRequest request) {
		String internal = request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER).trim();
		SecurityTokenResult result = internalTokenProvider.parseToken(internal);

		String principalType = result.getPrincipalType();

		// 用户身份令牌：从缓存重建完整用户画像
		if (SecurityInternalTokenConstants.PRINCIPAL_TYPE_USER.equals(principalType)) {
			Long userId = result.getUserToken() != null ? result.getUserToken().getUserId() : null;
			return loadUserProfile(userId, result.getPermVersion());
		}

		// 服务身份令牌：构建服务身份画像
		if (SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE.equals(principalType)) {
			return buildServiceProfile(result.getServiceId());
		}

		throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID,
				"Unknown principal_type: " + principalType);
	}

	/**
	 * 从缓存加载用户画像，支持版本校验
	 * @param userId 用户ID
	 * @param tokenPermVersion 令牌中的权限版本号
	 * @return 用户画像
	 */
	private AuthProfile loadUserProfile(Long userId, Long tokenPermVersion) {
		if (userId == null) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID,
					"User principal token must contain valid userId");
		}

		AuthProfile profile = authProfileCacheService.load(userId);

		if (profile == null) {
			throw new SecurityTokenException(SecurityResultCodeEnum.PROFILE_CACHE_MISS,
					"User profile not found in cache: userId=" + userId);
		}

		// 版本校验：防止权限变更后仍使用旧令牌
		if (tokenPermVersion != null && profile.getPermVersion() != null
				&& !tokenPermVersion.equals(profile.getPermVersion())) {
			throw new SecurityTokenException(SecurityResultCodeEnum.PERMISSION_VERSION_MISMATCH,
					"Permission version mismatch: token=" + tokenPermVersion + ", cache=" + profile.getPermVersion());
		}

		return profile;
	}

	/**
	 * 构造服务身份的最小化 AuthProfile：不绑定 userId/sessionId/permVersion，仅承载内部服务身份
	 */
	private AuthProfile buildServiceProfile(String serviceId) {
		String username = CharSequenceUtil.isNotBlank(serviceId) ? serviceId : "unknown-service";
		return AuthProfile.builder()
			.username(username)
			.roles(List.of(SecurityInternalTokenConstants.ROLE_INTERNAL_SERVICE))
			.build();
	}

}
