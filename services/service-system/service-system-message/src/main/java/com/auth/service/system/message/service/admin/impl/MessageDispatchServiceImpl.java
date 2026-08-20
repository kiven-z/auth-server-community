package com.auth.service.system.message.service.admin.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.config.MessageChannelCapability;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.service.admin.MessageDispatchService;
import com.auth.service.system.message.support.template.ChannelDefaultsEnricher;
import com.auth.service.system.message.support.template.TemplateMessageCommandValidation;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_CHANNEL_DISABLED;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_CHANNEL_UNSUPPORTED;

/**
 * 消息发送编排服务实现
 *
 * @author Bunny
 */
@Service
public class MessageDispatchServiceImpl implements MessageDispatchService {

	private final Map<MessageChannel, MessageSender> senderByChannel;

	private final Map<MessageChannel, MessageChannelCapability> capabilityByChannel;

	private final TemplateMessageCommandValidation commandValidation;

	private final ChannelDefaultsEnricher channelDefaultsEnricher;

	public MessageDispatchServiceImpl(List<MessageSender> senders, List<MessageChannelCapability> capabilities,
			TemplateMessageCommandValidation commandValidation, ChannelDefaultsEnricher channelDefaultsEnricher) {
		this.senderByChannel = indexSenders(senders);
		this.capabilityByChannel = indexCapabilities(capabilities);
		this.commandValidation = commandValidation;
		this.channelDefaultsEnricher = channelDefaultsEnricher;
	}

	/**
	 * 索引发送器
	 * @param senders 发送器列表
	 * @return 发送器映射
	 */
	private static Map<MessageChannel, MessageSender> indexSenders(List<MessageSender> senders) {
		Map<MessageChannel, MessageSender> indexed = new EnumMap<>(MessageChannel.class);
		for (MessageSender sender : senders) {
			indexed.put(sender.channel(), sender);
		}
		return indexed;
	}

	/**
	 * 索引渠道能力
	 * @param capabilities 渠道能力列表
	 * @return 渠道能力映射
	 */
	private static Map<MessageChannel, MessageChannelCapability> indexCapabilities(
			List<MessageChannelCapability> capabilities) {
		Map<MessageChannel, MessageChannelCapability> indexed = new EnumMap<>(MessageChannel.class);
		for (MessageChannelCapability capability : capabilities) {
			MessageChannel channel = capability.channel();
			MessageChannelCapability previous = indexed.put(channel, capability);
			if (previous != null) {
				throw new IllegalStateException("Duplicate MessageChannelCapability for channel: " + channel);
			}
		}
		return indexed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendByTemplate(TemplateMessageCommand command) {
		commandValidation.validate(command);
		channelDefaultsEnricher.enrich(command);
		MessageChannel channel = Objects.requireNonNull(command.getChannel(), "channel");
		MessageChannelCapability capability = capabilityByChannel.get(channel);
		// 未注册 capability 的渠道视为启用
		if (capability != null && !capability.isEnabled()) {
			throw new MessageException(MESSAGE_CHANNEL_DISABLED, channel.name());
		}

		MessageSender sender = senderByChannel.get(channel);
		if (sender == null) {
			throw new MessageException(MESSAGE_CHANNEL_UNSUPPORTED, channel.name());
		}
		sender.sendByTemplate(command);
	}

}
