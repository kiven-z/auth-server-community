package com.auth.service.system.authorization.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 授权失效 Outbox 运维结果码
 *
 * @author Bunny
 */
@Getter
public enum AuthorizationInvalidationOpsResultCode implements SystemResultCode {

	/**
	 * Outbox 当前状态不允许人工重试
	 */
	RETRY_NOT_ALLOWED(400, 299, "AUTH_INVALIDATION_OUTBOX_RETRY_NOT_ALLOWED",
			"system.authorization.invalidation.outbox.retry_not_allowed"),

	/**
	 * Outbox 人工重试并发冲突（版本或状态已变更）
	 */
	RETRY_CONFLICT(409, 300, "AUTH_INVALIDATION_OUTBOX_RETRY_CONFLICT",
			"system.authorization.invalidation.outbox.retry_conflict"),

	/**
	 * Outbox 仍处于 PROCESSING 且未满足解锁条件
	 */
	PROCESSING_LOCKED(409, 301, "AUTH_INVALIDATION_OUTBOX_PROCESSING_LOCKED",
			"system.authorization.invalidation.outbox.processing_locked"),

	/**
	 * 幂等事件非 processing 占位，无需释放
	 */
	EVENT_RELEASE_NOT_PROCESSING(400, 304, "AUTH_INVALIDATION_EVENT_RELEASE_NOT_PROCESSING",
			"system.authorization.invalidation.event.release_not_processing"),

	/**
	 * Outbox 人工重试投递失败
	 */
	RETRY_FAILED(200, 303, "AUTH_INVALIDATION_OUTBOX_RETRY_FAILED",
			"system.authorization.invalidation.outbox.retry_failed"),;

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	AuthorizationInvalidationOpsResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
