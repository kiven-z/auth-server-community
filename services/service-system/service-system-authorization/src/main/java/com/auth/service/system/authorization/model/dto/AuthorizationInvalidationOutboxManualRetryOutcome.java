package com.auth.service.system.authorization.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Outbox 人工重试执行摘要
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class AuthorizationInvalidationOutboxManualRetryOutcome {

	/**
	 * Outbox 主键
	 */
	private Long outboxId;

	/**
	 * 业务事件 ID
	 */
	private String eventId;

	/**
	 * 重试前状态
	 */
	private String previousStatus;

	/**
	 * 当前状态
	 */
	private String currentStatus;

	/**
	 * 最近失败原因
	 */
	private String lastError;

	/**
	 * 是否已完成一次投递尝试
	 */
	private boolean dispatched;

	/**
	 * 是否释放了 auth 侧 processing 占位
	 */
	private boolean claimReleased;

}
