package com.auth.service.system.message.channel.sms.dypns;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.channel.sms.dypns.transport.AliyunDypnsTransport;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.exception.MessageResultCode;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import com.auth.service.system.message.model.value.sms.dypns.DypnsVerifyCodeSendPayload;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DypnsVerifyCodeMessageSender} 单元测试
 *
 * @author Bunny
 */
@DisplayName("DypnsVerifyCodeMessageSender 短信发送")
@ExtendWith(MockitoExtension.class)
class DypnsVerifyCodeMessageSenderTest {

	@Mock
	private DypnsVerifyCodeTemplatePipeline dypnsVerifyCodeTemplatePipeline;

	@Mock
	private AliyunDypnsTransport aliyunDypnsTransport;

	@Mock
	private ChannelDeliveryRecorder deliveryRecorder;

	@InjectMocks
	private DypnsVerifyCodeMessageSender sender;

	@Test
	@DisplayName("单号发送成功：回写各自 BizId")
	void sendByTemplate_shouldRecordProviderBizId() {
		// 短信发送成功后把厂商 BizId 写入该目标的投递结果
		when(dypnsVerifyCodeTemplatePipeline.prepare(any())).thenReturn(DypnsVerifyCodeSendPayload.builder()
			.providerTemplateCode("TPL001")
			.templateParam("{\"code\":\"1234\"}")
			.build());
		when(aliyunDypnsTransport.sendVerifyCode("13800000000", "TPL001", "{\"code\":\"1234\"}"))
			.thenReturn("biz-9001");

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("verify")
			.targets(List.of("13800000000"))
			.variables(Map.of("code", "1234"))
			.build();

		sender.sendByTemplate(command);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TargetSendOutcome>> outcomesCaptor = ArgumentCaptor.forClass(List.class);
		verify(deliveryRecorder).recordPending(any(), eq(MessageChannel.SMS), eq(command.getTargets()));
		verify(deliveryRecorder).recordOutcomes(any(), eq(MessageChannel.SMS), outcomesCaptor.capture());
		assertThat(outcomesCaptor.getValue()).hasSize(1);
		assertThat(outcomesCaptor.getValue().get(0).getProviderMsgId()).isEqualTo("biz-9001");
		assertThat(outcomesCaptor.getValue().get(0).getTarget()).isEqualTo("13800000000");
	}

	@Test
	@DisplayName("多号发送：每个手机号各自保留 BizId")
	void sendByTemplate_shouldKeepDistinctBizIdPerPhone() {
		// 多目标时不得共用最后一个 BizId
		when(dypnsVerifyCodeTemplatePipeline.prepare(any())).thenReturn(DypnsVerifyCodeSendPayload.builder()
			.providerTemplateCode("TPL001")
			.templateParam("{\"code\":\"1234\"}")
			.build());
		when(aliyunDypnsTransport.sendVerifyCode("13800000001", "TPL001", "{\"code\":\"1234\"}")).thenReturn("biz-a");
		when(aliyunDypnsTransport.sendVerifyCode("13800000002", "TPL001", "{\"code\":\"1234\"}")).thenReturn("biz-b");

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("verify")
			.targets(List.of("13800000001", "13800000002"))
			.variables(Map.of("code", "1234"))
			.build();

		sender.sendByTemplate(command);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TargetSendOutcome>> outcomesCaptor = ArgumentCaptor.forClass(List.class);
		verify(deliveryRecorder).recordOutcomes(any(), eq(MessageChannel.SMS), outcomesCaptor.capture());
		assertThat(outcomesCaptor.getValue()).extracting(TargetSendOutcome::getTarget)
			.containsExactly("13800000001", "13800000002");
		assertThat(outcomesCaptor.getValue()).extracting(TargetSendOutcome::getProviderMsgId)
			.containsExactly("biz-a", "biz-b");
	}

	@Test
	@DisplayName("部分号码失败：成功号保留 BizId，整体抛 MESSAGE_DELIVERY_FAILED")
	void sendByTemplate_shouldKeepSuccessBizIdWhenPartialFail() {
		// 一号失败不影响另一号已成功回执落库
		when(dypnsVerifyCodeTemplatePipeline.prepare(any())).thenReturn(DypnsVerifyCodeSendPayload.builder()
			.providerTemplateCode("TPL001")
			.templateParam("{\"code\":\"1234\"}")
			.build());
		when(aliyunDypnsTransport.sendVerifyCode("13800000001", "TPL001", "{\"code\":\"1234\"}")).thenReturn("biz-ok");
		when(aliyunDypnsTransport.sendVerifyCode("13800000002", "TPL001", "{\"code\":\"1234\"}"))
			.thenThrow(new MessageException(MessageResultCode.SMS_DYPNS_SEND_FAILED, "vendor down"));

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS)
			.templateCode("verify")
			.targets(List.of("13800000001", "13800000002"))
			.variables(Map.of("code", "1234"))
			.build();

		assertThatThrownBy(() -> sender.sendByTemplate(command)).isInstanceOf(MessageException.class)
			.satisfies(ex -> assertThat(((MessageException) ex).getResultCode())
				.isEqualTo(MessageResultCode.MESSAGE_DELIVERY_FAILED));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<TargetSendOutcome>> outcomesCaptor = ArgumentCaptor.forClass(List.class);
		verify(deliveryRecorder).recordOutcomes(any(), eq(MessageChannel.SMS), outcomesCaptor.capture());
		List<TargetSendOutcome> outcomes = outcomesCaptor.getValue();
		assertThat(outcomes.get(0).isSuccess()).isTrue();
		assertThat(outcomes.get(0).getProviderMsgId()).isEqualTo("biz-ok");
		assertThat(outcomes.get(1).isSuccess()).isFalse();
		assertThat(outcomes.get(1).getErrorCode()).isEqualTo("SMS_DYPNS_SEND_FAILED");
	}

}
