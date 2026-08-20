package com.auth.service.auth.support.session;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.module.security.contract.api.UserSessionIndex;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * 从 HTTP 请求上下文组装 {@link UserSessionIndex}。
 *
 * @author Bunny
 */
@Component
public class UserSessionIndexFactory {

	private final IpAddressService ipAddressService;

	private final JwtProperties jwtProperties;

	public UserSessionIndexFactory(IpAddressService ipAddressService, JwtProperties jwtProperties) {
		this.ipAddressService = ipAddressService;
		this.jwtProperties = jwtProperties;
	}

	/**
	 * 根据登录请求与会话标识构建会话索引。
	 * @param request HTTP 请求
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 * @param refreshToken 刷新令牌明文（仅用于哈希，不入库）
	 * @param rememberMe 是否记住我
	 * @return 会话索引
	 */
	public UserSessionIndex buildSessionIndex(@NotNull HttpServletRequest request, Long userId, String jti,
			String refreshToken, boolean rememberMe) {
		IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
		UserAgent userAgent = UserAgent.getUserAgent(request);

		UserSessionIndex userSessionIndex = new UserSessionIndex();
		userSessionIndex.setUserId(userId);
		userSessionIndex.setSessionId(jti);
		userSessionIndex.setIpAddress(ipInfo.getIpAddr());
		userSessionIndex.setIpRegion(ipInfo.getIpRegion());
		userSessionIndex.setDeviceType(userAgent.getDeviceType());
		userSessionIndex.setBrowserType(userAgent.getBrowser());
		userSessionIndex.setOsType(userAgent.getOs());

		String refreshTokenHash = MD5.create().digestHex16(refreshToken);
		userSessionIndex.setRefreshTokenHash(refreshTokenHash);
		long refreshExpiredSeconds = jwtProperties.getRefreshExpired();
		long refreshTokenExpiresAtMillis = System.currentTimeMillis() + refreshExpiredSeconds * 1000L;
		userSessionIndex.setRefreshTokenExpiresAt(refreshTokenExpiresAtMillis);
		userSessionIndex.setRememberMe(rememberMe);
		userSessionIndex.setLoginAt(System.currentTimeMillis());
		return userSessionIndex;
	}

}
