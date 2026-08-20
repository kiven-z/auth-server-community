package com.auth.module.security.autoconfigure.security;

/**
 * 安全要求枚举
 *
 * @author Bunny
 */
public enum SecurityRequirement {

	/**
	 * 公共接口 @PublicApi
	 */
	PUBLIC,

	/**
	 * 内部接口 @InternalApi
	 */
	INTERNAL,

	/**
	 * 认证接口 @AuthenticatedApi
	 */
	AUTHENTICATED,

	/**
	 * 无注解 / 取不到 handler
	 */
	FALLBACK_TO_PATH,

}
