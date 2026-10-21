package com.auth.common.jwt.api;

import com.auth.common.jwt.model.JwtUserToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;

/**
 * JWT 契约：调用方只依赖本接口，算法实现由自动配置切换
 *
 * @author Bunny
 */
public interface JwtTokenProvider {

	/**
	 * 签发 Token
	 * @param builder 已填声明的构建器
	 * @return Token
	 */
	String generatorJwtToken(JwtBuilder builder);

	/**
	 * 验证 Token
	 * @param token Token
	 * @return 是否有效
	 */
	boolean validateToken(String token);

	/**
	 * 解析 Token
	 * @param token Token
	 * @return 用户信息
	 */
	JwtUserToken parseToken(String token);

	/**
	 * 获取 Claims
	 * @param token Token
	 * @return Claims
	 */
	Claims getClaims(String token);

	/**
	 * 获取剩余秒数
	 * @param token Token
	 * @return 剩余秒数
	 */
	long getRemainingSeconds(String token);

}
