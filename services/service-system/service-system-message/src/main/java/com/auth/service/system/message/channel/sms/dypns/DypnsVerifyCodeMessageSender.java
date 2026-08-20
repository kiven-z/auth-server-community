package com.auth.service.system.message.channel.sms.dypns;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.channel.AbstractRecordingMessageSender;
import com.auth.service.system.message.channel.sms.dypns.transport.AliyunDypnsTransport;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.value.delivery.ChannelSendResult;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import com.auth.service.system.message.model.value.sms.dypns.DypnsVerifyCodeSendPayload;
import com.auth.service.system.message.support.delivery.ChannelDeliveryRecorder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.SMS_CUSTOM_BODY_NOT_SUPPORTED;

/**
 * 阿里云 Dypns 验证码短信发送实现（一号一发、各自回执）
 *
 * @author Bunny
 */
@Component
public class DypnsVerifyCodeMessageSender extends AbstractRecordingMessageSender {

	private final DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline;

	private final AliyunDypnsTransport aliyunDypnsTransport;

	public DypnsVerifyCodeMessageSender(DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline,
			AliyunDypnsTransport aliyunDypnsTransport, ChannelDeliveryRecorder deliveryRecorder) {
		super(deliveryRecorder);
		this.dypnsVerifyCodeTemplatePipeline = dypnsVerifyCodeTemplatePipeline;
		this.aliyunDypnsTransport = aliyunDypnsTransport;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.SMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ChannelSendResult doSend(TemplateMessageCommand command) {
		if (command.getCustomBody() != null && command.getCustomBody()) {
			throw new MessageException(SMS_CUSTOM_BODY_NOT_SUPPORTED);
		}

		DypnsVerifyCodeSendPayload prepared = dypnsVerifyCodeTemplatePipeline.prepare(command);
		Instant sentAt = Instant.now();
		List<TargetSendOutcome> outcomes = command.getTargets()
			.stream()
			.map(phone -> sendOne(phone, prepared, sentAt))
			.toList();
		return ChannelSendResult.of(outcomes);
	}

	/**
	 * 单号发送；失败时转为目标级失败结果，不中断其余号码
	 * @param phone 手机号
	 * @param prepared 已准备的发送载荷
	 * @param sentAt 发送时间
	 * @return 该号码的发送结果
	 */
	private TargetSendOutcome sendOne(String phone, DypnsVerifyCodeSendPayload prepared, Instant sentAt) {
		try {
			String providerTemplateCode = prepared.providerTemplateCode();
			String templateParamJson = prepared.templateParam();
			String providerMsgId = aliyunDypnsTransport.sendVerifyCode(phone, providerTemplateCode, templateParamJson);

			return TargetSendOutcome.success(phone, providerMsgId, sentAt);
		}
		catch (RuntimeException ex) {
			String errorCode = ex.getClass().getSimpleName();
			if (ex instanceof MessageException me) {
				errorCode = me.getResultCode().getError();
			}
			String errorMessage = CharSequenceUtil.subPre(ex.getMessage(), ERROR_MESSAGE_MAX_LENGTH);
			return TargetSendOutcome.failure(phone, errorCode, errorMessage, sentAt);
		}
	}

}
