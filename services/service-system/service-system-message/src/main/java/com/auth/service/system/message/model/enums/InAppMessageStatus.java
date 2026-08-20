package com.auth.service.system.message.model.enums;

/**
 * 消息发送任务状态
 *
 * @author Bunny
 */
public enum InAppMessageStatus {

	/**
	 * 待发送
	 */
	PENDING,

	/**
	 * 发送中
	 */
	SENDING,

	/**
	 * 全部成功
	 */
	SUCCESS,

	/**
	 * 部分成功
	 */
	PARTIAL,

	/**
	 * 失败
	 */
	FAILED,

	/**
	 * 展开后无有效接收人
	 */
	NO_RECIPIENTS,

	/**
	 * 已撤回
	 */
	RECALLED;

	/**
	 * 解析任务状态（大小写不敏感）
	 * @param raw 原始字符串
	 * @return 枚举；无法识别时返回 NULL
	 */
	public static InAppMessageStatus from(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return InAppMessageStatus.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * 是否允许补发重置
	 * @return true=可重置为 PENDING
	 */
	public boolean isRetryable() {
		return this == PENDING || this == SENDING || this == PARTIAL || this == FAILED || this == NO_RECIPIENTS;
	}

	/**
	 * 是否允许撤回
	 * @return true=可置为 RECALLED
	 */
	public boolean isRecallable() {
		return this == SUCCESS || this == PARTIAL || this == NO_RECIPIENTS;
	}

	/**
	 * 是否允许物理删除
	 * @return true=可删除
	 */
	public boolean isDeletable() {
		return this != PENDING && this != SENDING;
	}

}
