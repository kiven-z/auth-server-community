package com.auth.common.web.exception;

import lombok.experimental.UtilityClass;

/**
 * common-starter-web 基础设施异常的稳定错误标识（写入 {@code Result.error} / {@code subCode}）
 *
 * @author Bunny
 */
@UtilityClass
public class CommonWebErrorCodes {

	/** 请求参数 / 校验失败 */
	public static final String VALIDATION_FAILED = "VALIDATION_FAILED";

	/** 请求体不可读（畸形 JSON 等） */
	public static final String UNREADABLE_BODY = "UNREADABLE_BODY";

	/** HTTP 方法不支持 */
	public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";

	/** 未捕获的服务端错误 */
	public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

	/** 数据库不可用或通用持久化失败 */
	public static final String DATABASE_UNAVAILABLE = "DATABASE_UNAVAILABLE";

	/** 字段值过长 */
	public static final String DATA_TOO_LONG = "DATA_TOO_LONG";

	/** 唯一约束冲突 */
	public static final String DUPLICATE_ENTRY = "DUPLICATE_ENTRY";

	/** 远端服务不可用或无业务信封 */
	public static final String UPSTREAM_UNAVAILABLE = "UPSTREAM_UNAVAILABLE";

}
