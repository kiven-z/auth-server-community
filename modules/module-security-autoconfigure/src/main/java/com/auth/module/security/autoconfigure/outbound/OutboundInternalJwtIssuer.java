package com.auth.module.security.autoconfigure.outbound;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.core.token.provider.InternalTokenProvider;

import java.util.UUID;

/**
 * 出站内部 JWT 签发：按当前安全上下文选择 USER 或 SERVICE 身份。
 *
 * @author Bunny
 */
public final class OutboundInternalJwtIssuer {

	private final InternalTokenProvider internalTokenProvider;

	private final String serviceId;

	/**
	 * @param internalTokenProvider 内部令牌签发器
	 * @param serviceId 当前服务名（一般为 spring.application.name）
	 */
	public OutboundInternalJwtIssuer(InternalTokenProvider internalTokenProvider, String serviceId) {
		this.internalTokenProvider = internalTokenProvider;
		this.serviceId = serviceId;
	}

	/**
	 * 按当前安全上下文签发内部 JWT：有用户则 USER，否则 SERVICE
	 * @return 内部 JWT 字符串
	 */
	public String issueInternalToken() {
		String jti = UUID.randomUUID().toString();
		AuthProfile currentUser = SecurityUserUtils.currentAuthProfile();
		if (currentUser != null && currentUser.getUserId() != null) {
			return internalTokenProvider.buildToken(currentUser.getUserId(), jti, currentUser.getPermVersion());
		}
		return internalTokenProvider.buildServiceToken(serviceId, jti);
	}

}
