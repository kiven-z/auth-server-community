package com.auth.service.system.message.channel;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.value.delivery.ChannelSendResult;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import com.auth.service.system.message.support.delivery.ChannelDeliveryRecorder;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.time.Instant;
import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_DELIVERY_FAILED;

/**
 * 渠道发送统一记录模板：投递结果按目标落库，允许部分成功
 *
 * @author Bunny
 */
public abstract class AbstractRecordingMessageSender implements MessageSender {

	/**
	 * 错误信息最大长度
	 */
	protected static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

	private final ChannelDeliveryRecorder deliveryRecorder;

	/**
	 * @param deliveryRecorder 投递记录器
	 */
	protected AbstractRecordingMessageSender(ChannelDeliveryRecorder deliveryRecorder) {
		this.deliveryRecorder = deliveryRecorder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void sendByTemplate(TemplateMessageCommand command) {
		Long taskId = IdWorker.getId();
		List<String> targets = command.getTargets();
		deliveryRecorder.recordPending(taskId, channel(), targets);

		ChannelSendResult result;
		try {
			result = doSend(command);
		}
		catch (RuntimeException ex) {
			// doSend 抛异常时整批标记失败（尚未产生目标级结果）
			String errorCode = ex.getClass().getSimpleName();
			if (ex instanceof MessageException me) {
				errorCode = me.getResultCode().getError();
			}
			String errorMessage = CharSequenceUtil.subPre(ex.getMessage(), ERROR_MESSAGE_MAX_LENGTH);
			deliveryRecorder.recordFailed(taskId, channel(), targets, errorCode, errorMessage, Instant.now());
			throw ex;
		}

		List<TargetSendOutcome> outcomes = result != null ? result.outcomes() : List.of();
		deliveryRecorder.recordOutcomes(taskId, channel(), outcomes);

		// 任一目标失败则抛出业务异常（成功目标的回执已落库）
		long failedCount = outcomes.stream().filter(outcome -> !outcome.isSuccess()).count();
		if (failedCount > 0) {
			throw new MessageException(MESSAGE_DELIVERY_FAILED, failedCount, outcomes.size());
		}
	}

	/**
	 * 发送实际业务逻辑
	 * @param command 模板发送命令
	 * @return 目标级发送结果
	 */
	protected abstract ChannelSendResult doSend(TemplateMessageCommand command);

}
