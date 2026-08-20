package com.auth.service.system.message.channel.inapp;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.value.inapp.RenderedInAppMessage;
import com.auth.service.system.message.support.template.MessageTemplateLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_VARS_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link InAppTemplatePipeline} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppTemplatePipeline 站内信模板渲染")
@ExtendWith(MockitoExtension.class)
class InAppTemplatePipelineTest {

	@Mock
	private MessageTemplateLoader messageTemplateLoader;

	@InjectMocks
	private InAppTemplatePipeline pipeline;

	@Test
	@DisplayName("渲染：填充变量并解析 MARKDOWN 正文类型")
	void render_shouldProcessTitleBodyAndContentType() {
		// 准备启用模板，校验变量替换与 contentType
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setId(55L);
		template.setSceneCode("notice");
		template.setTemplateName("notice");
		template.setSubject("Hello ${name}");
		template.setBodyContent("Body ${name}");
		template.setImMessageType("MARKDOWN");
		template.setRequireFields("[{\"key\":\"name\",\"exampleValue\":\"Bunny\"}]");
		when(messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, "notice")).thenReturn(template);

		RenderedInAppMessage rendered = pipeline.render("notice", Map.of("name", "Bunny"));

		assertThat(rendered.title()).isEqualTo("Hello Bunny");
		assertThat(rendered.content()).isEqualTo("Body Bunny");
		assertThat(rendered.contentType()).isEqualTo(MessageContentType.MARKDOWN);
		assertThat(rendered.sceneCode()).isEqualTo("notice");
		assertThat(rendered.templateId()).isEqualTo(55L);
	}

	@Test
	@DisplayName("缺少必填变量：抛出 TEMPLATE_VARS_MISSING")
	void render_shouldThrowWhenRequiredVariableMissing() {
		// require_fields 声明 name，变量未传时拒绝渲染
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setSceneCode("notice");
		template.setTemplateName("notice");
		template.setSubject("Hello ${name}");
		template.setBodyContent("Body");
		template.setRequireFields("[{\"key\":\"name\",\"exampleValue\":\"Bunny\"}]");
		when(messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, "notice")).thenReturn(template);

		Map<String, Object> variables = Map.of();
		assertThatThrownBy(() -> pipeline.render("notice", variables)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(TEMPLATE_VARS_MISSING);
	}

	@Test
	@DisplayName("标题为空：抛出 PARAM_REQUIRED")
	void render_shouldThrowWhenSubjectBlank() {
		// 标题字段缺失视为模板配置错误
		MessageTemplateEntity template = new MessageTemplateEntity();
		template.setSceneCode("notice");
		template.setTemplateName("notice");
		template.setSubject(" ");
		template.setBodyContent("Body");
		template.setRequireFields("[]");
		when(messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, "notice")).thenReturn(template);

		Map<String, Object> variables = Map.of();
		assertThatThrownBy(() -> pipeline.render("notice", variables)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

}
