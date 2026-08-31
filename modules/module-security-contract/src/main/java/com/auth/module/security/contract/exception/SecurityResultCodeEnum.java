package com.auth.module.security.contract.exception;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * SecurityResultCodeEnum 是安全模块的公共业务代码，用于表示安全模块的错误状态 (module=00).
 *
 * <ul>
 * <li>HTTP 状态码保持语义性 (401/403/503...)</li>
 * <li>Result.code 使用稳定的业务代码</li>
 * <li>Result.error 使用本枚举 {@link #getError()} 的稳定字符串</li>
 * <li>Security 契约层不进行 i18n 翻译，仅提供英文 {@link #defaultMessage} 与 {@link #i18nKey}
 * 供集成层/前端</li>
 * </ul>
 *
 * @author Bunny
 */
@Getter
public enum SecurityResultCodeEnum {

	// 00 + 4 + xx : 认证/授权失败
	/**
	 * 令牌缺失
	 */
	TOKEN_MISSING(401, 400, "TOKEN_MISSING", "security.token.missing", "Token is missing."),
	/**
	 * 令牌无效
	 */
	TOKEN_INVALID(401, 401, "TOKEN_INVALID", "security.token.invalid", "Token is invalid."),
	/**
	 * 令牌过期
	 */
	TOKEN_EXPIRED(401, 402, "TOKEN_EXPIRED", "security.token.expired", "Token has expired."),
	/**
	 * 会话缺失
	 */
	SESSION_MISSING(401, 403, "SESSION_MISSING", "security.session.missing", "Session is missing."),
	/**
	 * 会话禁用
	 */
	SESSION_DISABLED(401, 404, "SESSION_DISABLED", "security.session.disabled", "Session is disabled."),
	/**
	 * 访问拒绝
	 */
	ACCESS_DENIED(403, 405, "ACCESS_DENIED", "security.access.denied", "Access denied."),
	/**
	 * 权限版本不匹配
	 */
	PERMISSION_VERSION_MISMATCH(409, 406, "PERMISSION_VERSION_MISMATCH", "security.permission.version.mismatch",
			"Permission version mismatch, please refresh."),
	/**
	 * 会话数据不一致
	 */
	SESSION_INCONSISTENT(401, 407, "SESSION_INCONSISTENT", "security.session.inconsistent",
			"Session data inconsistent, please re-authenticate."),
	/**
	 * 并发会话数超过限制
	 */
	SESSION_LIMIT_EXCEEDED(401, 408, "SESSION_LIMIT_EXCEEDED", "security.session.limit.exceeded",
			"Concurrent session limit exceeded."),
	/**
	 * 令牌种类与期望不匹配（如内部/外部 Token 混用）
	 */
	TOKEN_KIND_MISMATCH(401, 409, "TOKEN_KIND_MISMATCH", "security.token.kind.mismatch",
			"Token kind is not supported."),
	/**
	 * 未认证（当前请求未建立有效认证主体；不等同于令牌格式错误或验签失败）
	 */
	NOT_AUTHENTICATED(401, 410, "NOT_AUTHENTICATED", "security.not.authenticated", "Not authenticated."),

	/**
	 * 未知错误（兜底；{@link #getError()} 与 {@link #TOKEN_INVALID} 相同以保持历史默认行为）
	 */
	UNKNOWN(401, 499, "TOKEN_INVALID", "security.unauthorized", "Unauthorized."),

	// 00 + 1 + xx : 系统错误
	/**
	 * Redis 不可用
	 */
	REDIS_UNAVAILABLE(503, 101, "REDIS_UNAVAILABLE", "security.redis.unavailable", "Redis is unavailable."),

	/**
	 * 授权画像反序列化失败
	 */
	PROFILE_DESERIALIZE_FAILED(500, 102, "PROFILE_DESERIALIZE_FAILED", "security.profile.deserialize.failed",
			"Failed to deserialize AuthProfile."),

	/**
	 * 用户画像缓存未命中（内部服务调用时，用户画像不在缓存中）
	 */
	PROFILE_CACHE_MISS(401, 103, "PROFILE_CACHE_MISS", "security.profile.cache.miss",
			"User profile not found in cache."),;

	private final int httpStatus;

	private final int bizCode;

	private final String error;

	private final String i18nKey;

	private final String defaultMessage;

	SecurityResultCodeEnum(int httpStatus, int bizCode, String error, String i18nKey, String defaultMessage) {
		this.httpStatus = httpStatus;
		this.bizCode = bizCode;
		this.error = error;
		this.i18nKey = i18nKey;
		this.defaultMessage = defaultMessage;
	}

	/**
	 * 从稳定 error 子码解析为枚举；无法识别时返回 {@link #UNKNOWN}。
	 * @param error 错误子码（与 {@link #getError()} 一致）
	 * @return 对应枚举
	 */
	public static SecurityResultCodeEnum fromError(String error) {
		for (SecurityResultCodeEnum candidate : values()) {
			if (candidate == UNKNOWN) {
				continue;
			}
			if (error.equals(candidate.getError())) {
				return candidate;
			}
		}
		return UNKNOWN;
	}

	/**
	 * 扩展信息
	 * @param args 参数
	 * @return 扩展信息
	 */
	public Map<String, Object> ext(Object... args) {
		return Map.of("i18nKey", i18nKey, "i18nArgs", Objects.requireNonNullElse(args, new Object[0]));
	}

}
