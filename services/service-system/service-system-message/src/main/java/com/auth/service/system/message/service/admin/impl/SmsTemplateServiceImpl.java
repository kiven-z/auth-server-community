package com.auth.service.system.message.service.admin.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.auth.service.system.message.service.admin.SmsTemplateService;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 短信模板服务实现
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SmsTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements SmsTemplateService {

	private static void applyEntity(SmsTemplateForm form, MessageTemplateEntity exists) {
		exists.setSceneCode(form.getTemplateCode());
		exists.setTemplateName(form.getTemplateName());
		exists.setProviderTemplateCode(form.getProviderTemplateCode());
		exists.setDescription(form.getDescription());
		exists.setStatus(form.getStatus());
		exists.setPriority(form.getPriority());
		exists.setBodyContent(form.getContent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SmsTemplateForm form) {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setChannel(MessageChannel.SMS.name());
		applyEntity(form, entity);
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(Collections.emptyList()));

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SmsTemplateForm form) {
		MessageTemplateEntity exists = getById(form.getId());
		if (exists == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		applyEntity(form, exists);
		updateById(exists);
	}

}
