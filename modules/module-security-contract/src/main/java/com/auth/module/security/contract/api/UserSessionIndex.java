package com.auth.module.security.contract.api;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户会话索引
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserSessionIndex {

	private Long userId;

	/**
	 * 会话ID
	 */
	private String sessionId;

	/**
	 * 请求IP
	 */
	private String ipAddress;

	/**
	 * IP归属地
	 */
	private String ipRegion;

	/**
	 * 设备类型
	 */
	private String deviceType;

	/**
	 * 浏览器类型
	 */
	private String browserType;

	/**
	 * 操作系统类型
	 */
	private String osType;

	/**
	 * Refresh Token 的 Hash 值（用于刷新时身份校验）
	 */
	private String refreshTokenHash;

	/**
	 * Refresh Token 的过期时间戳（毫秒），用于快速判断是否过期
	 */
	private Long refreshTokenExpiresAt;

	/**
	 * 登录时是否勾选「记住我」（刷新时用于决定 Refresh Cookie 为持久或会话级）
	 */
	private Boolean rememberMe;

	/**
	 * 登录时间戳（毫秒），用于全局在线会话排序
	 */
	private Long loginAt;

}
