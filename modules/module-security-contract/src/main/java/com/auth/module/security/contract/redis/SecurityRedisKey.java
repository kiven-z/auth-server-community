package com.auth.module.security.contract.redis;

import lombok.Getter;

import java.time.Duration;

/**
 * Security 模块 Redis Key 枚举定义（跨服务约定）
 * <p>
 * 每个枚举常量封装了 Redis Key 前缀与默认过期时间， 通过 {@link #key(String)} / {@link #key(long)} 构建完整 Key。
 *
 * @author Bunny
 */
@Getter
public enum SecurityRedisKey {

	/**
	 * 用户当前登录的会话详情（Hash） 默认 TTL: 7 天（实际由 refreshToken 动态计算，此值作为兜底） key 后缀: jti 最好和
	 * JwtProperties 配置一致
	 */
	USER_SESSION("auth:security:user:session:", Duration.ofDays(7)),

	/**
	 * 用户活跃会话索引集合（Set） 默认 TTL: null（无固定过期，随内部 session 详情自适应清理） key 后缀: userId
	 */
	USER_SESSIONS("auth:security:user:sessions:", null),

	/**
	 * 在线用户索引（ZSet，member=userId 字符串，score=lastLoginAt 毫秒时间戳） 默认 TTL: null（无活跃会话时由 Java
	 * 侧移除）
	 */
	ONLINE_USERS("auth:security:online:users", null),

	/**
	 * 用户权限缓存（Value） 默认 TTL: 15 天 key 后缀: userId 最好和 JwtProperties 配置一致
	 */
	USER_PERM("auth:security:user:perm:", Duration.ofDays(7)),

	/**
	 * 登录失败计数（Value） 默认 TTL: 5 分钟（滑动窗口期；锁定后会改为 15 分钟） key 后缀: userId
	 */
	LOGIN_FAILURE_COUNT("auth:security:user:login:failure:count:", Duration.ofMinutes(5)),

	/**
	 * 登录验证码限流（Value） 默认 TTL: 5 分钟 key 后缀: principal（邮箱或手机号）
	 */
	LOGIN_CODE_LIMIT("auth:security:login:code:limit:", Duration.ofMinutes(5)),

	/**
	 * 邮箱验证码（Value） 默认 TTL: 10 分钟 key 后缀: email
	 */
	EMAIL_CODE("auth:security:login:email:code:", Duration.ofMinutes(10)),

	/**
	 * 手机短信验证码（Value） 默认 TTL: 10 分钟 key 后缀: 手机号
	 */
	SMS_CODE("auth:security:login:sms:code:", Duration.ofMinutes(10));

	private final String prefix;

	private final Duration defaultTtl;

	SecurityRedisKey(String prefix, Duration defaultTtl) {
		this.prefix = prefix;
		this.defaultTtl = defaultTtl;
	}

	/**
	 * 构建完整 Redis Key（字符串后缀）
	 * @param suffix 动态后缀（如 jti、email、principal）
	 * @return 完整 Redis Key
	 */
	public String key(String suffix) {
		return prefix + suffix;
	}

	/**
	 * 构建完整 Redis Key（数字后缀）
	 * @param suffix 动态后缀（如 userId）
	 * @return 完整 Redis Key
	 */
	public String key(long suffix) {
		return prefix + suffix;
	}

	/**
	 * 构建无动态后缀的固定 Redis Key（如全局 ZSet）
	 * @return 完整 Redis Key
	 */
	public String fixedKey() {
		return prefix;
	}

}