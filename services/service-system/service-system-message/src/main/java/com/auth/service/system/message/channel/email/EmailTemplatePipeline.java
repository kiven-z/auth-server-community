package com.auth.service.system.message.channel.email;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.value.email.RenderedEmail;
import com.auth.service.system.message.support.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.support.template.MessageTemplateLoader;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_VARS_MISSING;

/**
 * 邮件模板加载、变量校验与渲染
 *
 * @author Bunny
 */
@Component
public class EmailTemplatePipeline {

	private final MessageTemplateLoader messageTemplateLoader;

	public EmailTemplatePipeline(MessageTemplateLoader messageTemplateLoader) {
		this.messageTemplateLoader = messageTemplateLoader;
	}

	/**
	 * 按模板编码渲染邮件主题与正文
	 * @param templateCode 模板编码
	 * @param variables 模板变量
	 * @return 渲染结果
	 */
	public RenderedEmail render(String templateCode, Map<String, Object> variables) {
		MessageTemplateEntity template = messageTemplateLoader.loadEnabled(MessageChannel.EMAIL, templateCode);
		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);

		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(
				MessageTemplateRequireFieldsJsonSupport.parse(template.getRequireFields()), safeVariables,
				TEMPLATE_VARS_MISSING, MessageChannel.EMAIL.name());

		String templateName = template.getTemplateName() + "_subject";
		String processTemplate = FreemarkerTemplateRenderer.processTemplate(templateName, template.getSubject(),
				safeVariables);

		String bodyContent = template.getBodyContent();
		String body = FreemarkerTemplateRenderer.processTemplate(template.getTemplateName(), bodyContent,
				safeVariables);
		return RenderedEmail.builder().subject(processTemplate).body(body).build();
	}

}
