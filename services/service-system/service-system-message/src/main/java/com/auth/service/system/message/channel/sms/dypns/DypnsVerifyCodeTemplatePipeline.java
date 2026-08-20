package com.auth.service.system.message.channel.sms.dypns;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.value.sms.dypns.DypnsVerifyCodeSendPayload;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import com.auth.service.system.message.support.template.MessageTemplateLoader;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_VARS_MISSING;

/**
 * Dypns 验证码短信：模板加载、变量校验与发送参数组装
 *
 * @author Bunny
 */
@Component
public class DypnsVerifyCodeTemplatePipeline {

	private final MessageTemplateLoader messageTemplateLoader;

	public DypnsVerifyCodeTemplatePipeline(MessageTemplateLoader messageTemplateLoader) {
		this.messageTemplateLoader = messageTemplateLoader;
	}

	/**
	 * 构建模板参数
	 * @param requireFields 必填字段
	 * @param variables 变量
	 * @return 模板参数
	 */
	private static String buildTemplateParam(List<MessageTemplateRequireFieldRow> requireFields,
			Map<String, Object> variables) {
		Map<String, String> paramMap = new LinkedHashMap<>();

		// 有必填字段，遍历必填字段
		if (CollUtil.isNotEmpty(requireFields)) {
			for (MessageTemplateRequireFieldRow field : requireFields) {
				Object value = variables.get(field.getKey());
				paramMap.put(field.getKey(), Convert.toStr(value, ""));
			}
		}
		else {
			variables.forEach((key, value) -> paramMap.put(key, Convert.toStr(value, "")));
		}

		return JSONUtil.toJsonStr(paramMap);
	}

	/**
	 * 准备发送参数
	 * @param command 命令
	 * @return 发送参数
	 */
	public DypnsVerifyCodeSendPayload prepare(TemplateMessageCommand command) {
		String sceneCode = command.getTemplateCode();

		// 加载模板
		MessageTemplateEntity template = messageTemplateLoader.loadEnabled(MessageChannel.SMS, sceneCode);

		// 获取变量
		Map<String, Object> variables = Optional.ofNullable(command.getVariables()).orElseGet(LinkedHashMap::new);

		// 解析必填字段
		List<MessageTemplateRequireFieldRow> requireFields = MessageTemplateRequireFieldsJsonSupport
			.parse(template.getRequireFields());
		MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields, variables, TEMPLATE_VARS_MISSING,
				MessageChannel.SMS.name());

		// 构建模板参数
		String templateParam = buildTemplateParam(requireFields, variables);
		return DypnsVerifyCodeSendPayload.builder()
			.providerTemplateCode(template.getProviderTemplateCode())
			.templateParam(templateParam)
			.build();
	}

}
