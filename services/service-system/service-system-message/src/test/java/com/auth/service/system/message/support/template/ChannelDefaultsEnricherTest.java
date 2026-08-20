package com.auth.service.system.message.support.template;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.inapp.InAppChannelOptions;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * {@link ChannelDefaultsEnricher} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ChannelDefaultsEnricher 合并模板渠道默认")
@ExtendWith(MockitoExtension.class)
class ChannelDefaultsEnricherTest {

	@Mock
	private MessageTemplateLoader messageTemplateLoader;

	@InjectMocks
	private ChannelDefaultsEnricher enricher;

	@Test
	@DisplayName("customBody：不加载模板")
	void enrich_customBody_skipsLoader() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.customBody(Boolean.TRUE)
			.body("x")
			.targets(List.of("1"))
			.build();

		assertThat(enricher.enrich(command)).isSameAs(command);
		verify(messageTemplateLoader, never()).loadEnabled(any(), any());
	}

	@Test
	@DisplayName("无 templateCode：不加载模板")
	void enrich_blankTemplateCode_skipsLoader() {
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.targets(List.of("a@example.com"))
			.build();

		assertThat(enricher.enrich(command)).isSameAs(command);
		verify(messageTemplateLoader, never()).loadEnabled(any(), any());
	}

	@Test
	@DisplayName("模板无 defaults：保留请求 options")
	void enrich_blankDefaults_keepsRequestOptions() {
		MessageTemplateEntity template = new MessageTemplateEntity();
		when(messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, "notice")).thenReturn(template);
		InAppChannelOptions request = new InAppChannelOptions();
		request.setCategoryId(9L);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("1"))
			.options(request)
			.build();

		enricher.enrich(command);

		assertThat(command.getOptions()).isSameAs(request);
	}

	@Test
	@DisplayName("请求覆盖模板默认：小类用默认，链接用请求")
	void enrich_mergesRequestOverDefaults() {
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setChannelDefaultsJson("{\"categoryId\":104,\"linkUrl\":\"/from-template\"}");
		when(messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, "file-export-success")).thenReturn(template);
		InAppChannelOptions request = new InAppChannelOptions();
		request.setLinkUrl("/from-request");
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("file-export-success")
			.targets(List.of("1"))
			.options(request)
			.build();

		enricher.enrich(command);

		InAppChannelOptions merged = (InAppChannelOptions) command.getOptions();
		assertThat(merged.getCategoryId()).isEqualTo(104L);
		assertThat(merged.getLinkUrl()).isEqualTo("/from-request");
	}

	@Test
	@DisplayName("请求无 options：整份使用模板默认")
	void enrich_nullRequest_usesDefaults() {
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setChannelDefaultsJson("{\"categoryId\":104,\"linkUrl\":\"/personal/export-task\"}");
		when(messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, "notice")).thenReturn(template);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("notice")
			.targets(List.of("1"))
			.build();

		enricher.enrich(command);

		InAppChannelOptions merged = (InAppChannelOptions) command.getOptions();
		assertThat(merged.getCategoryId()).isEqualTo(104L);
		assertThat(merged.getLinkUrl()).isEqualTo("/personal/export-task");
	}

	@Test
	@DisplayName("模板不存在：向上抛出 TEMPLATE_NOT_FOUND")
	void enrich_missingTemplate_throws() {
		when(messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, "missing"))
			.thenThrow(new MessageException(TEMPLATE_NOT_FOUND, "IN_APP", "missing"));
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.IN_APP)
			.templateCode("missing")
			.targets(List.of("1"))
			.build();

		assertThatThrownBy(() -> enricher.enrich(command)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(TEMPLATE_NOT_FOUND);
	}

}
