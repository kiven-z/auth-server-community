package com.auth.module.security.autoconfigure.pipeline.authenticate;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 按 RequestAuthenticator 链执行认证并填充上下文
 *
 * @author Bunny
 */
public final class SecurityAuthExecutor {

	/**
	 * 内部认证器（仅 X-Internal-JWT）
	 */
	private final InternalRequestAuthenticator internalRequestAuthenticator;

	/**
	 * 认证器列表
	 */
	private final List<RequestAuthenticator> authenticators;

	/**
	 * 授权画像安全上下文填充器
	 */
	private final AuthProfileSecurityContextPopulator populator;

	public SecurityAuthExecutor(InternalRequestAuthenticator internalRequestAuthenticator,
			List<RequestAuthenticator> authenticators, AuthProfileSecurityContextPopulator populator) {
		this.internalRequestAuthenticator = internalRequestAuthenticator;
		this.authenticators = authenticators;
		this.populator = populator;
	}

	/**
	 * 必须认证：找不到适配器或认证失败则抛异常
	 * @param request HTTP 请求
	 */
	public void require(HttpServletRequest request) {
		RequestAuthenticator authenticator = authenticators.stream()
			.filter(a -> a.supports(request))
			.findFirst()
			.orElseThrow(() -> new SecurityTokenException(SecurityResultCodeEnum.TOKEN_MISSING, "Token is missing."));
		AuthProfile authenticate = authenticator.authenticate(request);
		populator.populate(authenticate);
	}

	/**
	 * 必须内部认证：一刀切仅允许 X-Internal-JWT，禁止外部 Bearer
	 * @param request HTTP 请求
	 */
	public void requireInternal(HttpServletRequest request) {
		if (!internalRequestAuthenticator.supports(request)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_MISSING, "Internal token is missing.");
		}
		AuthProfile profile = internalRequestAuthenticator.authenticate(request);
		populator.populate(profile);
	}

	/**
	 * 尝试认证：有适配器则认证并填充上下文；否则静默跳过
	 * @param request HTTP 请求
	 */
	public void tryAuthenticate(HttpServletRequest request) {
		authenticators.stream()
			.filter(a -> a.supports(request))
			.findFirst()
			.ifPresent(a -> populator.populate(a.authenticate(request)));
	}

}
