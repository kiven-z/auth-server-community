package com.auth.module.security.core.token.provider;

import com.auth.module.security.core.token.model.SecurityTokenResult;

/**
 * 抽象 Token 支持
 *
 * @author Bunny
 */
public interface SecurityTokenProvider {

	/**
	 * 构建令牌
	 * @param userId 用户ID
	 * @param jti 令牌ID
	 * @param permVersion 权限版本
	 * @return 令牌
	 */
	String buildToken(Long userId, String jti, Long permVersion);

	/**
	 * 验证令牌是否过期、是否可用、是否是对应的类型
	 * @param token 令牌
	 * @return 是否有效
	 */
	boolean verifyToken(String token);

	/**
	 * 解析令牌
	 * @param token 令牌
	 * @return 解析结果
	 */
	SecurityTokenResult parseToken(String token);

}
