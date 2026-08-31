package com.auth.service.auth.exception;

import lombok.Getter;

/**
 * 统一返回结果状态信息类HTTP 语义以本枚举 {@link #httpStatus} 为唯一事实来源；构造参数顺序为 (httpStatus, code, error,
 * messageKey)，与 system 模块结果枚举风格一致
 *
 * @author Bunny
 */
@Getter
public enum AuthResultCode {

	/**
	 * 登录成功
	 */
	LOGIN_SUCCESS(200, 0, "SUCCESS", "result.login.success"),
	/**
	 * 退出登录成功
	 */
	SUCCESS_LOGOUT(200, 0, "SUCCESS", "result.success.logout"),

	/**
	 * 业务规则拒绝：账号密码错误
	 */
	USERNAME_OR_PASSWORD_ERROR(401, 1301, "BAD_CREDENTIALS", "username.or.password.error"),
	/**
	 * 业务规则拒绝：邮箱验证码错误（或验证码错误）
	 */
	AUTH_CODE_ERROR(401, 1302, "EMAIL_CODE_INVALID", "auth.code.error"),
	/**
	 * 认证/授权失败：Access Token 为空
	 */
	TOKEN_IS_NULL(401, 1411, "TOKEN_MISSING", "token.is.null"),

	/**
	 * 参数/格式错误：JSON 解析错误
	 */
	JSON_PARSER_EXCEPTION(400, 1202, "JSON_PARSER_ERROR", "json.parser.exception"),

	/**
	 * 参数/格式错误：email 没有收件人
	 */
	BAD_REQUEST_MISSING_EMAIL_EXCEPTION(400, 1203, "MISSING_EMAIL_RECIPIENT", "bad.request.missing.email.error"),

	/**
	 * 认证/授权失败：请先登陆（会话超时）
	 */
	LOGIN_AUTH(401, 1401, "LOGIN_TIMEOUT", "please.login.first"),
	/**
	 * 认证/授权失败：Token 解析失败
	 */
	TOKEN_PARSING_FAILED(401, 1410, "TOKEN_PARSING_FAILED", "token.parsing.failed"),

	/**
	 * 认证/授权失败：无权访问
	 */
	FAIL_NO_ACCESS_DENIED(403, 1403, "ACCESS_DENIED", "no.access.denied"),
	/**
	 * 业务规则拒绝：账号已锁定
	 */
	USER_LOCKED(403, 1305, "USER_LOCKED", "user.locked"),
	/**
	 * 业务规则拒绝：账号已锁定（带剩余锁定时间，分钟）
	 */
	USER_LOCKED_WITH_MINUTES(403, 1305, "USER_LOCKED", "user.locked.with.minutes"),
	/**
	 * 业务规则拒绝：账号已禁用
	 */
	USER_DISABLED(403, 1306, "USER_DISABLED", "user.disabled"),

	/**
	 * 认证失败：用户不存在（登录时通常以“凭证错误”对外隐藏；该码更多用于内部/管理端）
	 */
	USER_NOT_FOUND(401, 1404, "USER_NOT_FOUND", "user.not.found"),

	/**
	 * 资源不存在
	 */
	DATA_NOT_EXIST(404, 1206, "DATA_NOT_EXIST", "data.not.exists"),

	/**
	 * 业务规则拒绝：操作频繁
	 */
	TOO_MANY_REQUESTS(429, 1304, "TOO_MANY_REQUESTS", "too.many.requests"),
	/**
	 * 业务规则拒绝：账号密码错误（含剩余尝试次数）
	 */
	USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING(401, 1301, "BAD_CREDENTIALS",
			"username.or.password.error.with.remaining"),
	/**
	 * 业务规则拒绝：活跃会话数已达上限
	 */
	MAX_SESSION_LIMIT_EXCEEDED(403, 1307, "MAX_SESSION_LIMIT_EXCEEDED", "max.session.limit.exceeded"),
	/**
	 * 认证/授权失败：当前会话被新设备登录挤下线
	 */
	SESSION_KICKED(401, 1424, "SESSION_KICKED", "session.kicked"),

	/**
	 * 参数/格式错误：认证类型不支持
	 */
	AUTH_TYPE_UNSUPPORTED(400, 1303, "AUTH_TYPE_UNSUPPORTED", "auth.type.unsupported"),
	/**
	 * 参数/格式错误：认证请求类型不支持
	 */
	AUTH_REQUEST_TYPE_UNSUPPORTED(400, 1204, "AUTH_REQUEST_TYPE_UNSUPPORTED", "auth.request.type.unsupported"),

	/**
	 * 认证/授权失败：Refresh Token 缺失
	 */
	REFRESH_TOKEN_MISSING(401, 1423, "REFRESH_TOKEN_MISSING", "refresh.token.missing"),
	/**
	 * 认证/授权失败：Refresh Token 已过期/无效/会话异常
	 */
	REFRESH_TOKEN_EXPIRED(401, 1421, "REFRESH_TOKEN_EXPIRED", "refresh.token.expired"),
	/**
	 * 认证/授权失败：Refresh Token 格式错误
	 */
	REFRESH_TOKEN_MALFORMED(401, 1422, "REFRESH_TOKEN_MALFORMED", "refresh.token.malformed"),

	/**
	 * 数据库异常
	 */
	DATABASE_ERROR(500, 1101, "DATABASE_UNAVAILABLE", "database.error"),
	REDIS_UNAVAILABLE(500, 1102, "REDIS_UNAVAILABLE", "redis.unavailable"),
	SERVER_ERROR(500, 1199, "SERVER_ERROR", "server.error"),

	/**
	 * 服务不可用 503 服务不可用
	 */
	SERVICE_UNAVAILABLE(503, 1103, "SERVICE_UNAVAILABLE", "service.unavailable");

	/**
	 * 建议的 HTTP 状态码（与
	 * {@link AuthBusinessExceptionResultBuilder#resolveHttpStatus(AuthResultCode)} 一致）
	 */
	private final int httpStatus;

	/**
	 * 业务码
	 */
	private final Integer code;

	/**
	 * 稳定错误标识符（用于 Result.error）
	 */
	private final String error;

	/**
	 * i18n 消息键
	 */
	private final String messageKey;

	/**
	 * @param httpStatus 建议 HTTP 状态码
	 * @param code 业务码
	 * @param error 稳定错误标识
	 * @param messageKey i18n 键
	 */
	AuthResultCode(int httpStatus, Integer code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
