package com.auth.service.system.message.service.admin.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.channel.MessageSender;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.config.MessageChannelCapability;
import com.auth.service.system.message.config.properties.SmsProperties;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.support.template.ChannelDefaultsEnricher;
import com.auth.service.system.message.support.template.TemplateMessageCommandValidation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link MessageDispatchServiceImpl} 单元测试
 */
@DisplayName("MessageDispatchServiceImpl 消息渠道路由")
@ExtendWith(MockitoExtension.class)
class MessageDispatchServiceImplTest {

	@Mock
	private MessageSender emailSender;

	@Mock
	private MessageSender dingTalkSender;

	@Mock
	private MessageSender inAppSender;

	private SmsProperties smsProperties;

	private TemplateMessageCommandValidation commandValidation;

	@Mock
	private ChannelDefaultsEnricher channelDefaultsEnricher;

	private MessageDispatchServiceImpl dispatchService;

	@BeforeEach
	void setUp() {
		when(emailSender.channel()).thenReturn(MessageChannel.EMAIL);
		when(dingTalkSender.channel()).thenReturn(MessageChannel.DING_TALK);
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		commandValidation = new TemplateMessageCommandValidation(validator);
		smsProperties = new SmsProperties();
		lenient().when(channelDefaultsEnricher.enrich(any())).thenAnswer(invocation -> invocation.getArgument(0));
		dispatchService = new MessageDispatchServiceImpl(List.of(emailSender, dingTalkSender), List.of(smsProperties),
				commandValidation, channelDefaultsEnricher);
	}

	@Test
	@DisplayName("EMAIL 渠道：委托对应 MessageSender")
	void sendByTemplate_shouldRouteToEmailSender() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.build();

		dispatchService.sendByTemplate(command);

		verify(channelDefaultsEnricher).enrich(command);
		verify(emailSender).sendByTemplate(command);
		verify(dingTalkSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("DING_TALK 渠道：委托对应 MessageSender")
	void sendByTemplate_shouldRouteToDingTalkSender() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.templateCode("alert")
			.targets(List.of("user-1"))
			.build();

		dispatchService.sendByTemplate(command);

		verify(dingTalkSender).sendByTemplate(command);
		verify(emailSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("渠道禁用：抛出 MESSAGE_CHANNEL_DISABLED")
	void sendByTemplate_shouldThrowWhenChannelDisabled() {
		// 禁用短信渠道，验证分发层统一拦截
		smsProperties.setEnabled(false);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.build();

		assertThatThrownBy(() -> dispatchService.sendByTemplate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_CHANNEL_DISABLED);

		verify(emailSender, never()).sendByTemplate(any());
		verify(dingTalkSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("启用但无 sender：抛出 MESSAGE_CHANNEL_UNSUPPORTED")
	void sendByTemplate_shouldThrowWhenChannelUnsupported() {
		// 保持渠道启用，验证 sender 缺失仍然是“不支持/未实现”
		smsProperties.setEnabled(true);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("100001")
			.targets(List.of("13800000000"))
			.build();

		assertThatThrownBy(() -> dispatchService.sendByTemplate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_CHANNEL_UNSUPPORTED);

		verify(emailSender, never()).sendByTemplate(any());
		verify(dingTalkSender, never()).sendByTemplate(any());
	}

	@Test
	@DisplayName("未注册 capability 的渠道：视为启用并委托 sender")
	void sendByTemplate_shouldTreatMissingCapabilityAsEnabled() {
		// 站内信等无凭证渠道可不声明开关；有 sender 时应正常发出
		when(inAppSender.channel()).thenReturn(MessageChannel.IN_APP);
		MessageDispatchServiceImpl service = new MessageDispatchServiceImpl(List.of(inAppSender), List.of(),
				commandValidation, channelDefaultsEnricher);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("user-1"))
			.build();

		service.sendByTemplate(command);

		verify(inAppSender).sendByTemplate(command);
	}

	@Test
	@DisplayName("重复注册同一渠道 capability：构造失败")
	void constructor_shouldRejectDuplicateCapability() {
		SmsProperties first = new SmsProperties();
		SmsProperties second = new SmsProperties();
		List<MessageSender> senders = List.of(emailSender);
		List<MessageChannelCapability> capabilities = List.of(first, second);

		assertThatThrownBy(
				() -> new MessageDispatchServiceImpl(senders, capabilities, commandValidation, channelDefaultsEnricher))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("SMS");
	}

	@Test
	@DisplayName("command 为 null：抛出 MessageException")
	void sendByTemplate_shouldThrowWhenCommandNull() {
		assertThatThrownBy(() -> dispatchService.sendByTemplate(null)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

	@Test
	@DisplayName("targets 为空：抛出 MESSAGE_COMMAND_INVALID")
	void sendByTemplate_shouldThrowWhenTargetsEmpty() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(Collections.emptyList())
			.build();

		assertThatThrownBy(() -> dispatchService.sendByTemplate(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(MESSAGE_COMMAND_INVALID);

		verify(emailSender, never()).sendByTemplate(any());
	}

}
