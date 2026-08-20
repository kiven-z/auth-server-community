package com.auth.service.system.message.model.value.delivery;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 单个投递目标的发送结果（回执与状态挂在目标上，不共享批次回执）
 *
 * @author Bunny
 */
@Getter
public class TargetSendOutcome {

	/**
	 * 投递目标
	 */
	private final String target;

	/**
	 * 是否发送成功
	 */
	private final boolean success;

	/**
	 * 厂商回执 ID（成功时）
	 */
	private final String providerMsgId;

	/**
	 * 错误码（失败时）
	 */
	private final String errorCode;

	/**
	 * 错误信息（失败时）
	 */
	private final String errorMessage;

	/**
	 * 实际发送时间
	 */
	private final Instant sentAt;

	/**
	 * @param target 投递目标
	 * @param success 是否成功
	 * @param providerMsgId 厂商回执 ID
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @param sentAt 发送时间
	 */
	private TargetSendOutcome(String target, boolean success, String providerMsgId, String errorCode,
			String errorMessage, Instant sentAt) {
		this.target = Objects.requireNonNull(target, "target");
		this.success = success;
		this.providerMsgId = providerMsgId;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.sentAt = Objects.requireNonNullElseGet(sentAt, Instant::now);
	}

	/**
	 * 构造成功结果
	 * @param target 投递目标
	 * @param providerMsgId 厂商回执 ID
	 * @param sentAt 发送时间
	 * @return 成功结果
	 */
	public static TargetSendOutcome success(String target, String providerMsgId, Instant sentAt) {
		return new TargetSendOutcome(target, true, providerMsgId, null, null, sentAt);
	}

	/**
	 * 构造失败结果
	 * @param target 投递目标
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @param sentAt 发送时间
	 * @return 失败结果
	 */
	public static TargetSendOutcome failure(String target, String errorCode, String errorMessage, Instant sentAt) {
		return new TargetSendOutcome(target, false, null, errorCode, errorMessage, sentAt);
	}

}
