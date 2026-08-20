package com.auth.service.system.authorization.dispatch.query;

import lombok.Builder;
import lombok.Value;

/**
 * 用户活跃会话快照（供 admin 个人中心等只读场景）。
 *
 * @author Bunny
 */
@Value
@Builder
public class UserSessionSnapshot {

	/**
	 * 会话 ID（jti）
	 */
	String sessionId;

	/**
	 * 请求 IP
	 */
	String ipAddress;

	/**
	 * IP 归属地
	 */
	String ipRegion;

	/**
	 * 设备类型
	 */
	String deviceType;

	/**
	 * 浏览器类型
	 */
	String browserType;

	/**
	 * 操作系统类型
	 */
	String osType;

	/**
	 * 是否记住我
	 */
	Boolean rememberMe;

	/**
	 * Refresh Token 过期时间戳（毫秒）
	 */
	Long refreshTokenExpiresAt;

	/**
	 * 登录时间戳（毫秒）
	 */
	Long loginAt;

}
