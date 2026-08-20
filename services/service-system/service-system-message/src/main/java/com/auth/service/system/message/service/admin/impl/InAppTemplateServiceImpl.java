package com.auth.service.system.message.service.admin.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.inapp.InAppTemplateForm;
import com.auth.service.system.message.service.admin.InAppTemplateService;
import com.auth.service.system.message.support.inapp.InAppMessageCategorySupport;
import com.auth.service.system.message.support.template.ChannelDefaultsJsonSupport;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 站内信模板服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class InAppTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements InAppTemplateService {

	private final InAppMessageCategorySupport categorySupport;

	/**
	 * 将表单字段写入实体（含渠道默认小类与跳转）
	 * @param form 站内信模板表单
	 * @param entity 目标实体
	 */
	private void applyForm(InAppTemplateForm form, MessageTemplateEntity entity) {
		MessageContentType contentType = MessageContentType.from(form.getContentType());
		InAppMessageCategoryEntity minor = categorySupport.requireEnabledMinor(form.getCategoryId());

		entity.setSceneCode(form.getTemplateCode());
		entity.setTemplateName(form.getTemplateName());
		entity.setSubject(form.getSubject());
		entity.setImMessageType(contentType.name());
		entity.setBodyContent(form.getContent());
		entity.setDescription(form.getDescription());
		entity.setStatus(form.getStatus());
		entity.setPriority(form.getPriority());
		String inAppJson = ChannelDefaultsJsonSupport.toInAppJson(minor.getId(), form.getLinkUrl());
		entity.setChannelDefaultsJson(inAppJson);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(InAppTemplateForm form) {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setChannel(MessageChannel.IN_APP.name());
		applyForm(form, entity);
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(Collections.emptyList()));

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(InAppTemplateForm form) {
		MessageTemplateEntity exists = getById(form.getId());
		if (exists == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		applyForm(form, exists);
		updateById(exists);
	}

}
