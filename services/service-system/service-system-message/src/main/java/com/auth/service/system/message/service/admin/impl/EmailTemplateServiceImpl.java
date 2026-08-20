package com.auth.service.system.message.service.admin.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.email.EmailTemplateContentForm;
import com.auth.service.system.message.model.form.email.EmailTemplateForm;
import com.auth.service.system.message.model.form.email.EmailTemplateRenderForm;
import com.auth.service.system.message.service.admin.EmailTemplateService;
import com.auth.service.system.message.support.template.FreemarkerTemplateRenderer;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 邮件模板服务实现
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EmailTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements EmailTemplateService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String render(EmailTemplateRenderForm form) {
		try {
			Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(form.getRequireFields());
			return FreemarkerTemplateRenderer.processTemplate("inline-preview", form.getContent(), model);
		}
		catch (Exception e) {
			return "预览渲染失败，请检查变量声明与正文语法";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(EmailTemplateForm form) {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setChannel(MessageChannel.EMAIL.name());
		entity.setSceneCode(form.getTemplateCode());
		entity.setTemplateName(form.getTemplateName());
		entity.setSubject(form.getSubject());
		entity.setDescription(form.getDescription());
		entity.setStatus(form.getStatus());
		entity.setPriority(form.getPriority());
		entity.setBodyContent(CharSequenceUtil.nullToDefault(form.getContent(), "Template content is empty"));
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(Collections.emptyList()));

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(EmailTemplateForm form) {
		MessageTemplateEntity exists = getById(form.getId());
		if (exists == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		exists.setSceneCode(form.getTemplateCode());
		exists.setTemplateName(form.getTemplateName());
		exists.setSubject(form.getSubject());
		exists.setDescription(form.getDescription());
		exists.setStatus(form.getStatus());
		exists.setPriority(form.getPriority());
		exists.setBodyContent(CharSequenceUtil.nullToDefault(form.getContent(), exists.getBodyContent()));
		updateById(exists);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateContent(EmailTemplateContentForm form) {
		MessageTemplateEntity exists = getById(form.getId());
		if (exists == null || !MessageChannel.EMAIL.name().equals(exists.getChannel())) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		exists.setBodyContent(form.getContent());
		updateById(exists);
	}

}
