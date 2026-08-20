package com.auth.service.system.message.support.inapp;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_RECIPIENT_SCOPE_INVALID;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_COMMAND_INVALID;

/**
 * 管理端站内信发送表单条件校验
 *
 * @author Bunny
 */
@UtilityClass
public class InAppComposeFormValidation {

	/**
	 * USER/POST/DEPT 必须带范围 ID；ALL 不强制
	 * @param type 范围类型
	 * @param form 表单
	 */
	private static void requireScopeIds(RecipientScopeType type, InAppComposeForm form) {
		if (type == RecipientScopeType.ALL) {
			return;
		}

		List<Long> recipientScopeIds = form.getRecipientScopeIds();
		if (CollUtil.isEmpty(recipientScopeIds)) {
			throw new MessageException(IN_APP_RECIPIENT_SCOPE_INVALID, type.name());
		}
	}

	/**
	 * 校验条件规则
	 * @param form 发送表
	 * @return 已解析的接收范围类型
	 */
	public static RecipientScopeType validate(InAppComposeForm form) {
		RecipientScopeType type = RecipientScopeType.from(form.getRecipientScopeType());
		if (type == null) {
			throw new MessageException(IN_APP_RECIPIENT_SCOPE_INVALID, form.getRecipientScopeType());
		}
		requireScopeIds(type, form);

		// 正文格式必须为已声明枚举
		String contentType = form.getContentType();
		if (MessageContentType.parseOrNull(contentType) == null) {
			throw new MessageException(MESSAGE_COMMAND_INVALID, contentType);
		}
		return type;
	}

}
