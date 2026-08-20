package com.auth.service.system.message.support.inapp;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.message.convert.InAppEntityAssembler;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.auth.service.system.message.service.admin.InAppMessageRecipientWriteService;
import com.auth.service.system.message.support.recipient.RecipientBatchScanner;
import com.auth.service.system.message.support.recipient.RecipientScopeJsonSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_RECIPIENT_SCOPE_INVALID;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_SEND_TASK_NOT_FOUND;

/**
 * 站内信群发任务执行
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class InAppComposeDispatcher {

	private final InAppMessageMapper inAppMessageMapper;

	private final RecipientBatchScanner recipientBatchScanner;

	private final InAppMessageRecipientWriteService inAppMessageRecipientWriteService;

	/**
	 * 执行站内信群发任务
	 * @param taskId 任务 ID
	 */
	public void execute(Long taskId) {
		boolean claimed = inAppMessageMapper.updateStatusCas(taskId, InAppMessageStatus.PENDING.name(),
				InAppMessageStatus.SENDING.name()) > 0;
		if (!claimed) {
			return;
		}

		InAppMessageEntity task = inAppMessageMapper.selectById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		String recipientScopeType = task.getRecipientScopeType();
		RecipientScopeType type = RecipientScopeType.from(recipientScopeType);
		if (type == null) {
			throw new MessageException(IN_APP_RECIPIENT_SCOPE_INVALID, recipientScopeType);
		}
		RecipientScope scope = RecipientScopeJsonSupport.fromJson(type, task.getRecipientScopeJson());

		try {
			// 投递单批接收人
			recipientBatchScanner.scan(scope, BatchSizes.SIZE_1000, userIds -> {
				if (CollUtil.isEmpty(userIds)) {
					return;
				}
				List<InAppMessageRecipientEntity> rows = InAppEntityAssembler.toRecipientEntities(taskId, userIds);
				inAppMessageRecipientWriteService.insertBatch(rows);
			});
			finishAfterScan(taskId);
		}
		catch (RuntimeException ex) {
			finishOnFailure(taskId, ex);
			throw ex;
		}
	}

	/**
	 * 扫描结束后按收件箱实投数写入终态
	 * @param taskId 任务 ID
	 */
	private void finishAfterScan(Long taskId) {
		int delivered = inAppMessageRecipientWriteService.countByMessageId(taskId);
		if (delivered <= 0) {
			inAppMessageMapper.finishTask(taskId, InAppMessageStatus.NO_RECIPIENTS.name(), 0);
			log.info("In-app compose finished with no recipients, taskId={}", taskId);
			return;
		}
		inAppMessageMapper.finishTask(taskId, InAppMessageStatus.SUCCESS.name(), delivered);
	}

	/**
	 * 中途失败：按收件箱实投数标记 PARTIAL / FAILED
	 * @param taskId 任务 ID
	 * @param ex 异常
	 */
	private void finishOnFailure(Long taskId, RuntimeException ex) {
		int delivered = inAppMessageRecipientWriteService.countByMessageId(taskId);
		InAppMessageStatus status = delivered > 0 ? InAppMessageStatus.PARTIAL : InAppMessageStatus.FAILED;
		inAppMessageMapper.finishTask(taskId, status.name(), Math.max(delivered, 0));
		log.warn("In-app compose dispatch failed, taskId={}, delivered={}, status={}", taskId, delivered, status, ex);
	}

}
