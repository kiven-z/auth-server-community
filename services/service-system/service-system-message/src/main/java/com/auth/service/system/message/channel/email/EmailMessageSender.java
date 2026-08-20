package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.message.channel.AbstractRecordingMessageSender;
import com.auth.service.system.message.channel.email.transport.MimeMailTransport;
import com.auth.service.system.message.model.dto.EmailSendDTO;
import com.auth.service.system.message.model.value.delivery.ChannelSendResult;
import com.auth.service.system.message.model.value.email.RenderedEmail;
import com.auth.service.system.message.support.delivery.ChannelDeliveryRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 电子邮件渠道发送实现
 *
 * @author Bunny
 */
@Slf4j
@Component
public class EmailMessageSender extends AbstractRecordingMessageSender {

	private final EmailTemplatePipeline emailTemplatePipeline;

	private final MimeMailTransport mimeMailTransport;

	public EmailMessageSender(EmailTemplatePipeline emailTemplatePipeline, MimeMailTransport mimeMailTransport,
			ChannelDeliveryRecorder deliveryRecorder) {
		super(deliveryRecorder);
		this.emailTemplatePipeline = emailTemplatePipeline;
		this.mimeMailTransport = mimeMailTransport;
	}

	/**
	 * 将统一命令转为邮件 DTO（仅 EMAIL 渠道有效）
	 * @param command 模板化发送命令
	 * @return 邮件发送请求
	 */
	private static EmailSendDTO toEmailDto(TemplateMessageCommand command) {
		EmailChannelOptions options = command.getOptions() instanceof EmailChannelOptions email ? email
				: new EmailChannelOptions();

		EmailSendDTO emailSendDTO = new EmailSendDTO();
		emailSendDTO.setTemplateCode(command.getTemplateCode());
		emailSendDTO.setTo(command.getTargets());
		emailSendDTO.setVariables(command.getVariables());
		emailSendDTO.setCc(options.getCc());
		emailSendDTO.setBcc(options.getBcc());
		emailSendDTO.setReplyTo(options.getReplyTo());
		emailSendDTO.setHasHtml(Objects.requireNonNullElse(options.getHasHtml(), Boolean.TRUE));
		emailSendDTO.setAttachments(options.getAttachments());
		return emailSendDTO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel channel() {
		return MessageChannel.EMAIL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ChannelSendResult doSend(TemplateMessageCommand command) {
		EmailSendDTO emailSendDTO = toEmailDto(command);
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(HashMap::new);
		RenderedEmail rendered = emailTemplatePipeline.render(command.getTemplateCode(), variables);

		String providerMsgId = mimeMailTransport.send(emailSendDTO, rendered);
		return ChannelSendResult.sharedSuccess(command.getTargets(), providerMsgId, Instant.now());
	}

}
