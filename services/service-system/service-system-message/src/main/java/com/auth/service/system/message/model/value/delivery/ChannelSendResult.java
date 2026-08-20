package com.auth.service.system.message.model.value.delivery;

import cn.hutool.core.collection.CollUtil;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 渠道发送结果
 *
 * @param outcomes 目标级发送结果
 * @author Bunny
 */
public record ChannelSendResult(List<TargetSendOutcome> outcomes) {

	public ChannelSendResult(List<TargetSendOutcome> outcomes) {
		this.outcomes = List.copyOf(Objects.requireNonNull(outcomes, "outcomes"));
	}

	/**
	 * 按目标结果列表构造
	 * @param outcomes 目标级结果
	 * @return 渠道发送结果
	 */
	public static ChannelSendResult of(List<TargetSendOutcome> outcomes) {
		return new ChannelSendResult(outcomes != null ? outcomes : List.of());
	}

	/**
	 * 多目标共享同一厂商回执（邮件 / 钉钉等一次 API 覆盖多个目标时）
	 * @param targets 投递目标
	 * @param providerMsgId 厂商回执 ID
	 * @param sentAt 发送时间
	 * @return 渠道发送结果
	 */
	public static ChannelSendResult sharedSuccess(Collection<String> targets, String providerMsgId, Instant sentAt) {
		if (CollUtil.isEmpty(targets)) {
			return of(List.of());
		}
		Instant at = Objects.requireNonNullElseGet(sentAt, Instant::now);
		List<TargetSendOutcome> list = targets.stream()
			.filter(Objects::nonNull)
			.map(target -> TargetSendOutcome.success(target, providerMsgId, at))
			.toList();
		return of(list);
	}

}
