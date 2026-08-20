package com.auth.service.system.common.exception.code;

import lombok.Getter;

/**
 * 系统服务通用结果码（成功、请求校验、用户、基础设施等）
 *
 * <p>
 * 号段：0、102–103、205–209、306–314、402、206
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemCommonResultCode implements SystemResultCode {

	/**
	 * 操作成功
	 */
	OPERATION_SUCCESS(200, 0, "OPERATION_SUCCESS", "operation.success"),

	/**
	 * 数据不存在
	 */
	DATA_NOT_EXIST(404, 206, "DATA_NOT_EXIST", "data.not.exists"),

	/**
	 * 当前数据不存在或不可用（含已停用、已删除）
	 */
	DATA_UNAVAILABLE(404, 306, "DATA_UNAVAILABLE", "data.unavailable"),

	/**
	 * 数据仍被引用，无法删除
	 */
	DATA_IN_USE(409, 307, "DATA_IN_USE", "data.in_use"),

	/**
	 * 编码已存在
	 */
	DATA_CODE_DUPLICATE(409, 308, "DATA_CODE_DUPLICATE", "data.code.duplicate"),

	/**
	 * 树形父节点不存在或不可用
	 */
	TREE_PARENT_UNAVAILABLE(404, 309, "TREE_PARENT_UNAVAILABLE", "tree.parent.unavailable"),

	/**
	 * 存在未删除的子节点
	 */
	TREE_HAS_ACTIVE_CHILDREN(409, 310, "TREE_HAS_ACTIVE_CHILDREN", "tree.has_active_children"),

	/**
	 * 关联数据无效或未启用
	 */
	GRANT_REFERENCE_INVALID(400, 311, "GRANT_REFERENCE_INVALID", "grant.reference_invalid"),

	/**
	 * 数据异常（格式、解析或完整性错误）
	 */
	DATA_INVALID(422, 312, "DATA_INVALID", "data.invalid"),

	/**
	 * 必填参数为空
	 */
	PARAM_REQUIRED(422, 313, "PARAM_REQUIRED", "param.required"),

	/**
	 * 参数重复
	 */
	PARAM_DUPLICATE(409, 314, "PARAM_DUPLICATE", "param.duplicate"),

	/**
	 * 操作失败（外部请求、渲染等）
	 */
	OPERATION_FAILED(502, 315, "OPERATION_FAILED", "operation.failed"),

	/**
	 * 验证码错误
	 */
	VERIFY_CODE_ERROR(422, 205, "VERIFY_CODE_ERROR", "verify.code.error"),

	/**
	 * 用户邮箱未绑定
	 */
	USER_EMAIL_NOT_BIND(422, 208, "USER_EMAIL_NOT_BIND", "user.email.not.bind"),

	/**
	 * 用户不存在
	 */
	USER_NOT_FOUND(404, 402, "USER_NOT_FOUND", "user.not.found"),

	/**
	 * 服务不可用
	 */
	SERVICE_UNAVAILABLE(503, 102, "SERVICE_UNAVAILABLE", "service.unavailable"),

	/**
	 * JWT 无效/过期
	 */
	JWT_INVALID(401, 405, "JWT_INVALID", "system.jwt.invalid"),

	/**
	 * Redis 基础设施错误
	 */
	REDIS_SYSTEM_ERROR(503, 103, "REDIS_SYSTEM_ERROR", "system.redis.unavailable");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemCommonResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
