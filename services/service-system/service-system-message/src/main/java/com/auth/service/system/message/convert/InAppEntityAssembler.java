package com.auth.service.system.message.convert;

import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.MessageSendSourceType;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.value.inapp.RenderedInAppMessage;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.auth.service.system.message.support.recipient.RecipientScopeJsonSupport;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 站内信实体组装
 *
 * @author Bunny
 */
@UtilityClass
public class InAppEntityAssembler {

	/**
	 * 管理端起草任务
	 * @param form 表单
	 * @param scope 范围
	 * @param contentType 正文类型
	 * @param senderUserId 发送人
	 * @return 待插入任务
	 */
	public static InAppMessageEntity toAdminComposeTask(InAppComposeForm form, RecipientScope scope,
			MessageContentType contentType, Long senderUserId) {
		boolean pull = scope.getType().isPull();
		InAppMessageEntity task = new InAppMessageEntity();
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setSceneCode(form.getTemplateCode());
		task.setTitle(form.getTitle());
		task.setContentType(contentType.name());
		task.setContent(form.getBody());
		task.setCategoryId(form.getCategoryId());
		task.setLinkUrl(form.getLinkUrl());
		task.setSenderUserId(senderUserId);
		task.setRecipientScopeType(scope.getType().name());
		task.setRecipientScopeJson(RecipientScopeJsonSupport.toJson(scope));
		task.setTotalCount(0);
		task.setSuccessCount(0);
		task.setFailCount(0);
		task.setStatus(pull ? InAppMessageStatus.SUCCESS.name() : InAppMessageStatus.PENDING.name());
		return task;
	}

	/**
	 * 系统/模板渠道直发任务
	 * @param rendered 定稿快照
	 * @param userIds 目标用户 ID 列表
	 * @return 待插入任务
	 */
	public static InAppMessageEntity toDirectTask(RenderedInAppMessage rendered, List<Long> userIds) {
		RecipientScope scope = RecipientScope.builder().type(RecipientScopeType.USER).ids(userIds).build();
		InAppMessageEntity task = new InAppMessageEntity();
		task.setSourceType(rendered.sourceType().name());
		task.setSceneCode(rendered.sceneCode());
		task.setTitle(rendered.title());
		task.setContentType(rendered.contentType().name());
		task.setContent(rendered.content());
		task.setCategoryId(rendered.categoryId());
		task.setLinkUrl(rendered.linkUrl());
		task.setRecipientScopeType(scope.getType().name());
		task.setRecipientScopeJson(RecipientScopeJsonSupport.toJson(scope));
		task.setTotalCount(userIds.size());
		task.setSuccessCount(0);
		task.setFailCount(0);
		task.setStatus(InAppMessageStatus.SENDING.name());
		return task;
	}

	/**
	 * 按用户列表批量组装收件箱行
	 * @param messageId 站内信 ID
	 * @param userIds 接收人
	 * @return 待插入实体列表
	 */
	public static List<InAppMessageRecipientEntity> toRecipientEntities(Long messageId, List<Long> userIds) {
		return userIds.stream().map(userId -> {
			InAppMessageRecipientEntity row = new InAppMessageRecipientEntity();
			row.setMessageId(messageId);
			row.setUserId(userId);
			row.setIsRead(Boolean.FALSE);
			row.setIsDeleted(Boolean.FALSE);
			return row;
		}).toList();
	}

}
