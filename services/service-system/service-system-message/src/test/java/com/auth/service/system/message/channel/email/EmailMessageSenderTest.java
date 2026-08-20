package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.channel.email.transport.MimeMailTransport;
import com.auth.service.system.message.model.dto.EmailSendDTO;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import com.auth.service.system.message.model.value.email.RenderedEmail;
import com.auth.service.system.message.support.delivery.ChannelDeliveryRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link EmailMessageSender} 单元测试
 *
 * @author Bunny
 */
@DisplayName("EmailMessageSender 邮件发送")
@ExtendWith(MockitoExtension.class)
class EmailMessageSenderTest {

	@Mock
	private EmailTemplatePipeline emailTemplatePipeline;

	@Mock
	private MimeMailTransport mimeMailTransport;

	@Mock
	private ChannelDeliveryRecorder deliveryRecorder;

	@InjectMocks
	private EmailMessageSender emailMessageSender;

	@Test
	@DisplayName("发送成功：回写 Message-ID 作为各目标 providerMsgId")
	void sendByTemplate_shouldRecordProviderMessageId() {
		// 邮件一次发送共享 Message-ID，按目标写入 outcomes
		when(emailTemplatePipeline.render(eq("login-email-code"), any()))
			.thenReturn(RenderedEmail.builder().subject("subject").body("<p>hi</p>").build());
		when(mimeMailTransport.send(any(EmailSendDTO.class), any(RenderedEmail.class)))
			.thenReturn("<msg-id@localhost>");

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("login-email-code")
			.targets(List.of("a@example.com"))
			.variables(Map.of("code", "123456"))
			.build();

		emailMessageSender.sendByTemplate(command);

		ArgumentCaptor<EmailSendDTO> dtoCaptor = ArgumentCaptor.forClass(EmailSendDTO.class);
		verify(mimeMailTransport).send(dtoCaptor.capture(), any(RenderedEmail.class));
		assertThat(dtoCaptor.getValue().getHasHtml()).isTrue();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TargetSendOutcome>> outcomesCaptor = ArgumentCaptor.forClass(List.class);
		verify(deliveryRecorder).recordPending(any(), eq(MessageChannel.EMAIL), eq(command.getTargets()));
		verify(deliveryRecorder).recordOutcomes(any(), eq(MessageChannel.EMAIL), outcomesCaptor.capture());
		assertThat(outcomesCaptor.getValue()).hasSize(1);
		assertThat(outcomesCaptor.getValue().get(0).getProviderMsgId()).isEqualTo("<msg-id@localhost>");
	}

}
