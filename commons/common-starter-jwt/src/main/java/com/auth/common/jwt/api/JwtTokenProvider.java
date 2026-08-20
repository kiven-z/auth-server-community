package com.auth.common.jwt.api;

import com.auth.common.jwt.model.JwtUserToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;

/**
 * JWT 契约：调用方只应依赖本接口；算法实现由自动配置切换 定义 JWT 操作的契约：生成、验证、解析、获取剩余时间等调用方只依赖此接口
 *
 * @author Bunny
 */
public interface JwtTokenProvider {

	/**
	 * 签名算法
	 * @param builder 构建器
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
