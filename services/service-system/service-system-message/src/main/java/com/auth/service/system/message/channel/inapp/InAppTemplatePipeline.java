package com.auth.service.system.message.channel.inapp;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.value.inapp.RenderedInAppMessage;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import com.auth.service.system.message.support.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.support.template.MessageTemplateLoader;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_VARS_MISSING;

/**
 * 站内信模板加载、变量校验与渲染
 *
 * @author Bunny
 */
@Component
public class InAppTemplatePipeline {

	private final MessageTemplateLoader messageTemplateLoader;

	public InAppTemplatePipeline(MessageTemplateLoader messageTemplateLoader) {
		this.messageTemplateLoader = messageTemplateLoader;
	}

	/**
	 * 按场景编码渲染站内信标题与正文
	 * @param sceneCode 场景编码（发送命令 templateCode）
	 * @param variables 模板变量
	 * @return 渲染结果
	 */
	public RenderedInAppMessage render(String sceneCode, Map<String, Object> variables) {
		MessageTemplateEntity template = messageTemplateLoader.loadEnabled(MessageChannel.IN_APP, sceneCode);

		Map<String, Object> safeVariables = Optional.ofNullable(variables).orElseGet(HashMap::new);
		List<MessageTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, safeVariables,
				TEMPLATE_VARS_MISSING, MessageChannel.IN_APP.name());

		String subject = template.getSubject();
		if (CharSequenceUtil.isBlank(subject)) {
			throw new MessageException(PARAM_REQUIRED, "站内信标题");
		}
		String bodyContent = template.getBodyContent();
		if (CharSequenceUtil.isBlank(bodyContent)) {
			throw new MessageException(PARAM_REQUIRED, "站内信正文");
		}

		String templateName = template.getTemplateName();
		String title = FreemarkerTemplateRenderer.processTemplate(templateName + "_title", subject, safeVariables);
		String content = FreemarkerTemplateRenderer.processTemplate(templateName, bodyContent, safeVariables);
		MessageContentType contentType = MessageContentType.from(template.getImMessageType());

		return RenderedInAppMessage.builder()
			.title(title)
			.content(content)
			.contentType(contentType)
			.sceneCode(template.getSceneCode())
			.templateId(template.getId())
			.build();
	}

}
