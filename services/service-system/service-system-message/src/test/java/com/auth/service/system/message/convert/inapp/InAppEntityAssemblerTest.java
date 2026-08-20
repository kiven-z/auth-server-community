package com.auth.service.system.message.convert.inapp;

import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.service.system.message.convert.InAppEntityAssembler;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.MessageSendSourceType;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.value.inapp.RenderedInAppMessage;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InAppEntityAssembler} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppEntityAssembler 站内信实体组装")
class InAppEntityAssemblerTest {

	@Test
	@DisplayName("管理端定向：写扩散 + PENDING")
	void toAdminComposeTask_pushScope_shouldMapPending() {
		// 校验发送任务字段与 PENDING/计数默认值
		InAppComposeForm form = new InAppComposeForm();
		form.setTemplateCode("notice");
		form.setTitle("Hello");
		form.setBody("body text");
		form.setCategoryId(104L);
		form.setLinkUrl("/home");

		RecipientScope scope = RecipientScope.builder()
			.type(RecipientScopeType.DEPT)
			.ids(List.of(3L))
			.includeChildren(true)
			.build();

		InAppMessageEntity task = InAppEntityAssembler.toAdminComposeTask(form, scope, MessageContentType.MARKDOWN, 7L);

		assertThat(task.getSourceType()).isEqualTo(MessageSendSourceType.ADMIN_COMPOSE.name());
		assertThat(task.getSceneCode()).isEqualTo("notice");
		assertThat(task.getTitle()).isEqualTo("Hello");
		assertThat(task.getContent()).isEqualTo("body text");
		assertThat(task.getContentType()).isEqualTo(MessageContentType.MARKDOWN.name());
		assertThat(task.getCategoryId()).isEqualTo(104L);
		assertThat(task.getLinkUrl()).isEqualTo("/home");
		assertThat(task.getSenderUserId()).isEqualTo(7L);
		assertThat(task.getRecipientScopeType()).isEqualTo(RecipientScopeType.DEPT.name());
		assertThat(task.getRecipientScopeJson()).contains("\"includeChildren\":true");
		assertThat(task.getStatus()).isEqualTo(InAppMessageStatus.PENDING.name());
		assertThat(task.getTotalCount()).isZero();
		assertThat(task.getSuccessCount()).isZero();
		assertThat(task.getFailCount()).isZero();
	}

	@Test
	@DisplayName("管理端全员：读扩散 + SUCCESS")
	void toAdminComposeTask_allScope_shouldMapPublished() {
		// 公开读扩散发布即成功，不进入 PENDING
		InAppComposeForm form = new InAppComposeForm();
		form.setTitle("Broadcast");
		form.setBody("hello all");

		RecipientScope scope = RecipientScope.builder().type(RecipientScopeType.ALL).build();
		InAppMessageEntity task = InAppEntityAssembler.toAdminComposeTask(form, scope, MessageContentType.TEXT, 1L);

		assertThat(task.getRecipientScopeType()).isEqualTo(RecipientScopeType.ALL.name());
		assertThat(task.getStatus()).isEqualTo(InAppMessageStatus.SUCCESS.name());
	}

	@Test
	@DisplayName("直发任务：SENDING 状态，范围快照按 USER 展开")
	void toDirectTask_shouldSnapshotUserScopeAndSending() {
		// 系统/模板渠道直发场景，任务直接进 SENDING，等待 Sender 收尾
		RenderedInAppMessage rendered = RenderedInAppMessage.builder()
			.sourceType(MessageSendSourceType.TEMPLATE)
			.sceneCode("notice")
			.title("Hello")
			.content("body")
			.contentType(MessageContentType.MARKDOWN)
			.categoryId(104L)
			.linkUrl("/home")
			.build();
		InAppMessageEntity task = InAppEntityAssembler.toDirectTask(rendered, List.of(1001L, 1002L));

		assertThat(task.getSourceType()).isEqualTo(MessageSendSourceType.TEMPLATE.name());
		assertThat(task.getSceneCode()).isEqualTo("notice");
		assertThat(task.getTitle()).isEqualTo("Hello");
		assertThat(task.getContent()).isEqualTo("body");
		assertThat(task.getContentType()).isEqualTo(MessageContentType.MARKDOWN.name());
		assertThat(task.getCategoryId()).isEqualTo(104L);
		assertThat(task.getLinkUrl()).isEqualTo("/home");
		assertThat(task.getRecipientScopeType()).isEqualTo(RecipientScopeType.USER.name());
		assertThat(task.getRecipientScopeJson()).contains("1001").contains("1002");
		assertThat(task.getTotalCount()).isEqualTo(2);
		assertThat(task.getStatus()).isEqualTo(InAppMessageStatus.SENDING.name());
	}

	@Test
	@DisplayName("收件箱：按 messageId+userIds 展开，仅带用户级默认状态")
	void toRecipientEntities_shouldExpandMessageIdPerUser() {
		// 只落 messageId/userId + is_read/is_deleted 默认值；正文不再冗余
		List<InAppMessageRecipientEntity> rows = InAppEntityAssembler.toRecipientEntities(9001L, List.of(1L, 2L));

		assertThat(rows).hasSize(2)
			.allMatch(row -> row.getMessageId() == 9001L)
			.allMatch(row -> Boolean.FALSE.equals(row.getIsRead()))
			.allMatch(row -> Boolean.FALSE.equals(row.getIsDeleted()));
		assertThat(rows).extracting(InAppMessageRecipientEntity::getUserId).containsExactly(1L, 2L);
	}

}
