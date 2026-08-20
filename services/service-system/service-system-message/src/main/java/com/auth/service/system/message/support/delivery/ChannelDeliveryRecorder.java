package com.auth.service.system.message.support.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.mapper.MessageChannelDeliveryMapper;
import com.auth.service.system.message.model.entity.MessageChannelDeliveryEntity;
import com.auth.service.system.message.model.enums.MessageDeliveryStatus;
import com.auth.service.system.message.model.value.delivery.ChannelDeliveryResultUpdate;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 渠道投递记录写入器
 *
 * @author Bunny
 */
@Service
public class ChannelDeliveryRecorder extends ServiceImpl<MessageChannelDeliveryMapper, MessageChannelDeliveryEntity> {

	/**
	 * 写入待发送记录
	 * @param taskId 任务 ID
	 * @param channel 逻辑渠道
	 * @param targets 投递目标
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void recordPending(Long taskId, MessageChannel channel, Collection<String> targets) {
		if (taskId == null || channel == null || CollUtil.isEmpty(targets)) {
			return;
		}
		List<MessageChannelDeliveryEntity> rows = targets.stream().filter(CharSequenceUtil::isNotBlank).map(target -> {
			MessageChannelDeliveryEntity entity = new MessageChannelDeliveryEntity();
			entity.setTaskId(taskId);
			entity.setChannel(channel.name());
			entity.setTargetValue(target);
			entity.setStatus(MessageDeliveryStatus.PENDING.name());
			entity.setRetryCount(0);
			entity.setVersion(0L);
			return entity;
		}).toList();
		if (rows.isEmpty()) {
			return;
		}
		super.saveBatch(rows, BatchSizes.SIZE_500);
	}

	/**
	 * 按目标逐条回写发送结果（每个目标可有独立 providerMsgId / 成败）
	 * @param taskId 任务 ID
	 * @param channel 逻辑渠道
	 * @param outcomes 目标级结果
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void recordOutcomes(Long taskId, MessageChannel channel, List<TargetSendOutcome> outcomes) {
		if (taskId == null || channel == null || CollUtil.isEmpty(outcomes)) {
			return;
		}
		List<ChannelDeliveryResultUpdate> updates = outcomes.stream()
			.filter(Objects::nonNull)
			.filter(outcome -> CharSequenceUtil.isNotBlank(outcome.getTarget()))
			.map(outcome -> new ChannelDeliveryResultUpdate().setTargetValue(outcome.getTarget())
				.setStatus(outcome.isSuccess() ? MessageDeliveryStatus.SUCCESS.name()
						: MessageDeliveryStatus.FAILED.name())
				.setProviderMsgId(outcome.getProviderMsgId())
				.setErrorCode(outcome.getErrorCode())
				.setErrorMessage(outcome.getErrorMessage())
				.setSentAt(outcome.getSentAt()))
			.toList();
		applyResultUpdates(taskId, channel, updates);
	}

	/**
	 * 整批回写失败结果（doSend 异常时尚无目标级结果）
	 * @param taskId 任务 ID
	 * @param channel 逻辑渠道
	 * @param targets 投递目标
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @param sentAt 实际发送时间
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void recordFailed(Long taskId, MessageChannel channel, Collection<String> targets, String errorCode,
			String errorMessage, Instant sentAt) {
		if (taskId == null || channel == null || CollUtil.isEmpty(targets)) {
			return;
		}
		List<ChannelDeliveryResultUpdate> updates = targets.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(target -> new ChannelDeliveryResultUpdate().setTargetValue(target)
				.setStatus(MessageDeliveryStatus.FAILED.name())
				.setErrorCode(errorCode)
				.setErrorMessage(errorMessage)
				.setSentAt(sentAt))
			.toList();
		applyResultUpdates(taskId, channel, updates);
	}

	/**
	 * 分片批量回写投递结果
	 * @param taskId 任务 ID
	 * @param channel 逻辑渠道
	 * @param updates 更新参数
	 */
	private void applyResultUpdates(Long taskId, MessageChannel channel, List<ChannelDeliveryResultUpdate> updates) {
		if (CollUtil.isEmpty(updates)) {
			return;
		}
		CollUtil.split(updates, BatchSizes.SIZE_500)
			.forEach(chunk -> baseMapper.batchUpdateResult(taskId, channel.name(), chunk));
	}

}
