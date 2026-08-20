package com.auth.service.system.message.channel.dingtalk;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.channel.dingtalk.client.DingTalkWorkNoticeClient;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import com.auth.service.system.message.model.value.dingtalk.RenderedDingTalkNotice;
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
 * {@link DingTalkMessageSender} 单元测试
 *
 * @author Bunny
 */
@DisplayName("DingTalkMessageSender 钉钉发送")
@ExtendWith(MockitoExtension.class)
class DingTalkMessageSenderTest {

	@Mock
	private DingTalkTemplatePipeline dingTalkTemplatePipeline;

	@Mock
	private DingTalkWorkNoticeClient workNoticeClient;

	@Mock
	private ChannelDeliveryRecorder deliveryRecorder;

	@InjectMocks
	private DingTalkMessageSender sender;

	@Test
	@DisplayName("发送成功：回写 task_id 作为各目标 providerMsgId")
	void sendByTemplate_shouldRecordProviderTaskId() {
		// 钉钉一次 API 共享 task_id，按目标写入 outcomes
		when(dingTalkTemplatePipeline.render("notice", Map.of("user", "Bunny")))
			.thenReturn(RenderedDingTalkNotice.builder()
				.messageType(MessageContentType.TEXT)
				.title("t")
				.content("body")
				.build());
		when(workNoticeClient.sendWorkNotice(List.of("u1"), MessageContentType.TEXT, "t", "body"))
			.thenReturn("1234567890");

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.DING_TALK)
			.templateCode("notice")
			.targets(List.of("u1"))
			.variables(Map.of("user", "Bunny"))
			.build();

		sender.sendByTemplate(command);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TargetSendOutcome>> outcomesCaptor = ArgumentCaptor.forClass(List.class);
		verify(deliveryRecorder).recordPending(any(), eq(MessageChannel.DING_TALK), eq(command.getTargets()));
		verify(deliveryRecorder).recordOutcomes(any(), eq(MessageChannel.DING_TALK), outcomesCaptor.capture());
		assertThat(outcomesCaptor.getValue()).hasSize(1);
		assertThat(outcomesCaptor.getValue().get(0).getProviderMsgId()).isEqualTo("1234567890");
	}

}
