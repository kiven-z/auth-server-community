package com.auth.service.system.message.channel;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.exception.MessageResultCode;
import com.auth.service.system.message.model.value.delivery.ChannelSendResult;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import com.auth.service.system.message.support.delivery.ChannelDeliveryRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link AbstractRecordingMessageSender} 单元测试
 */
@DisplayName("AbstractRecordingMessageSender 发送记录")
@ExtendWith(MockitoExtension.class)
class AbstractRecordingMessageSenderTest {

	@Mock
	private ChannelDeliveryRecorder recorder;

	@Test
	@DisplayName("发送成功：记录 pending 与 outcomes")
	void sendByTemplate_shouldRecordOutcomesOnSuccess() {
		// 验证成功流程：pending + recordOutcomes，且不整批失败
		AbstractRecordingMessageSender sender = new TestRecordingMessageSender(recorder, SendMode.SUCCESS);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("email-login")
			.targets(List.of("a@example.com", "b@example.com"))
			.build();

		sender.sendByTemplate(command);

		ArgumentCaptor<Long> taskIdCaptor = ArgumentCaptor.forClass(Long.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TargetSendOutcome>> outcomesCaptor = ArgumentCaptor.forClass(List.class);
		verify(recorder).recordPending(taskIdCaptor.capture(), eq(MessageChannel.EMAIL), eq(command.getTargets()));
		verify(recorder).recordOutcomes(eq(taskIdCaptor.getValue()), eq(MessageChannel.EMAIL),
				outcomesCaptor.capture());
		verify(recorder, never()).recordFailed(any(), any(), any(), any(), any(), any());
		assertThat(taskIdCaptor.getValue()).isNotNull();
		assertThat(outcomesCaptor.getValue()).hasSize(2)
			.allMatch(TargetSendOutcome::isSuccess)
			.extracting(TargetSendOutcome::getProviderMsgId)
			.containsOnly("provider-1");
	}

	@Test
	@DisplayName("doSend 抛异常：整批 failed，且不写 outcomes")
	void sendByTemplate_shouldRecordFailedWhenDoSendThrows() {
		// 验证异常路径：尚未产生目标级结果时整批标失败
		AbstractRecordingMessageSender sender = new TestRecordingMessageSender(recorder, SendMode.THROW);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("alert")
			.targets(List.of("u-1"))
			.build();

		assertThatThrownBy(() -> sender.sendByTemplate(command)).isInstanceOf(IllegalStateException.class);

		ArgumentCaptor<Long> taskIdCaptor = ArgumentCaptor.forClass(Long.class);
		verify(recorder).recordPending(taskIdCaptor.capture(), eq(MessageChannel.EMAIL), eq(command.getTargets()));
		verify(recorder).recordFailed(eq(taskIdCaptor.getValue()), eq(MessageChannel.EMAIL), eq(command.getTargets()),
				any(), any(), any(Instant.class));
		verify(recorder, never()).recordOutcomes(any(), any(), any());
		assertThat(taskIdCaptor.getValue()).isNotNull();
	}

	@Test
	@DisplayName("部分失败：outcomes 已落库后抛 MESSAGE_DELIVERY_FAILED")
	void sendByTemplate_shouldThrowWhenAnyTargetFailed() {
		// 验证部分成功：成功回执已记录，仍向上抛投递失败
		AbstractRecordingMessageSender sender = new TestRecordingMessageSender(recorder, SendMode.PARTIAL);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode("alert")
			.targets(List.of("ok@example.com", "bad@example.com"))
			.build();

		assertThatThrownBy(() -> sender.sendByTemplate(command)).isInstanceOf(MessageException.class)
			.satisfies(ex -> assertThat(((MessageException) ex).getResultCode())
				.isEqualTo(MessageResultCode.MESSAGE_DELIVERY_FAILED));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TargetSendOutcome>> outcomesCaptor = ArgumentCaptor.forClass(List.class);
		verify(recorder).recordOutcomes(any(), eq(MessageChannel.EMAIL), outcomesCaptor.capture());
		verify(recorder, never()).recordFailed(any(), any(), any(), any(), any(), any());
		assertThat(outcomesCaptor.getValue()).hasSize(2);
		assertThat(outcomesCaptor.getValue().get(0).isSuccess()).isTrue();
		assertThat(outcomesCaptor.getValue().get(1).isSuccess()).isFalse();
	}

	/**
	 * 测试发送模式
	 */
	private enum SendMode {

		SUCCESS, THROW, PARTIAL

	}

	/**
	 * 测试用的发送器实现
	 */
	private static final class TestRecordingMessageSender extends AbstractRecordingMessageSender {

		private final SendMode sendMode;

		private TestRecordingMessageSender(ChannelDeliveryRecorder recorder, SendMode sendMode) {
			super(recorder);
			this.sendMode = sendMode;
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
			if (sendMode == SendMode.THROW) {
				throw new IllegalStateException("send failed");
			}
			Instant now = Instant.now();
			if (sendMode == SendMode.PARTIAL) {
				return ChannelSendResult.of(List.of(TargetSendOutcome.success("ok@example.com", "provider-ok", now),
						TargetSendOutcome.failure("bad@example.com", "ERR", "boom", now)));
			}
			return ChannelSendResult.sharedSuccess(command.getTargets(), "provider-1", now);
		}

	}

}
