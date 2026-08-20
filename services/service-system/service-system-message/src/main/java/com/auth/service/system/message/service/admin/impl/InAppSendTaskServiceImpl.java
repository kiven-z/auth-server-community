package com.auth.service.system.message.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.model.enums.MessageContentType;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.InAppEntityAssembler;
import com.auth.service.system.message.convert.InAppSendTaskConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.mapper.InAppMessageUserStatusMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.MessageSendSourceType;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.po.InAppSendTaskPageRowPO;
import com.auth.service.system.message.model.po.InAppSendTaskRecipientPageRowPO;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.auth.service.system.message.model.query.InAppSendTaskRecipientQuery;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskRecipientPageVO;
import com.auth.service.system.message.service.admin.InAppMessageCategoryService;
import com.auth.service.system.message.service.admin.InAppSendTaskService;
import com.auth.service.system.message.support.inapp.InAppComposeDispatchTrigger;
import com.auth.service.system.message.support.inapp.InAppComposeFormValidation;
import com.auth.service.system.message.support.inapp.InAppMessageCategorySupport;
import com.auth.service.system.message.support.recipient.RecipientScopeJsonSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.auth.service.system.message.exception.MessageResultCode.*;

/**
 * 管理端站内信发送任务服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class InAppSendTaskServiceImpl extends ServiceImpl<InAppMessageMapper, InAppMessageEntity>
		implements InAppSendTaskService {

	private final InAppComposeDispatchTrigger dispatchTrigger;

	private final AuditUserDisplayService auditUserDisplayService;

	private final InAppMessageCategoryService inAppMessageCategoryService;

	private final InAppMessageCategorySupport categorySupport;

	private final InAppMessageRecipientMapper inAppMessageRecipientMapper;

	private final InAppMessageUserStatusMapper inAppMessageUserStatusMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<InAppSendTaskPageVO> getSendTaskPage(InAppSendTaskQuery query) {
		Page<InAppMessageEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<InAppSendTaskPageRowPO> page = baseMapper.selectSendTaskPage(pageParams, query);
		IPage<InAppSendTaskPageVO> voPage = page.convert(InAppSendTaskConverter.INSTANCE::toPageVO);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public InAppSendTaskDetailVO getSendTaskById(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		InAppSendTaskDetailVO vo = InAppSendTaskConverter.INSTANCE.toDetailVo(task);
		String scopeJson = task.getRecipientScopeJson();
		vo.setRecipientScopeIds(RecipientScopeJsonSupport.parseIds(scopeJson));
		vo.setIncludeChildren(RecipientScopeJsonSupport.parseIncludeChildren(scopeJson));
		if (task.getCategoryId() != null) {
			var category = inAppMessageCategoryService.getById(task.getCategoryId());
			vo.setCategoryName(category != null ? category.getName() : null);
		}
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<InAppSendTaskRecipientPageVO> getRecipientPage(Long taskId, InAppSendTaskRecipientQuery query) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		RecipientScopeType scopeType = RecipientScopeType.from(task.getRecipientScopeType());
		if (scopeType == null) {
			throw new MessageException(IN_APP_RECIPIENT_SCOPE_INVALID, task.getRecipientScopeType());
		}

		Page<InAppSendTaskRecipientPageRowPO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<InAppSendTaskRecipientPageRowPO> page = scopeType.isPull()
				? inAppMessageUserStatusMapper.selectUserStatusPage(pageParams, taskId, query)
				: inAppMessageRecipientMapper.selectRecipientPage(pageParams, taskId, query);
		IPage<InAppSendTaskRecipientPageVO> voPage = page.convert(InAppSendTaskConverter.INSTANCE::toRecipientPageVO);
		auditUserDisplayService.enrichAuditUsernames(voPage, row -> Objects.requireNonNullElse(row.getUserId(), 0L),
				InAppSendTaskRecipientPageVO::setUsername);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public InAppComposeResultVO send(InAppComposeForm form) {
		RecipientScopeType type = InAppComposeFormValidation.validate(form);
		categorySupport.requireEnabledMinor(form.getCategoryId());
		RecipientScope scope = RecipientScope.builder()
			.type(type)
			.ids(form.getRecipientScopeIds())
			.includeChildren(form.getIncludeChildren())
			.build();
		MessageContentType contentType = MessageContentType.from(form.getContentType());

		Long senderUserId = SecurityUserUtils.getUserId();
		InAppMessageEntity task = InAppEntityAssembler.toAdminComposeTask(form, scope, contentType, senderUserId);
		super.save(task);

		if (type.isPull()) {
			return InAppComposeResultVO.builder()
				.taskId(task.getId())
				.totalCount(0)
				.successCount(0)
				.status(InAppMessageStatus.SUCCESS.name())
				.build();
		}

		dispatchTrigger.dispatchAfterCommit(task.getId());
		return InAppComposeResultVO.builder()
			.taskId(task.getId())
			.totalCount(0)
			.successCount(0)
			.status(InAppMessageStatus.PENDING.name())
			.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void retry(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		InAppMessageStatus status = InAppMessageStatus.from(task.getStatus());
		boolean retryAllowed = MessageSendSourceType.ADMIN_COMPOSE.name().equals(task.getSourceType())
				&& RecipientScopeType.from(task.getRecipientScopeType()) != RecipientScopeType.ALL && status != null
				&& status.isRetryable();
		if (!retryAllowed) {
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		if (baseMapper.resetForRetry(taskId) <= 0) {
			// CAS 失败：并发下状态已变，避免双发
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		dispatchTrigger.dispatchAfterCommit(taskId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recall(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		InAppMessageStatus status = InAppMessageStatus.from(task.getStatus());
		if (status == null || !status.isRecallable()) {
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		Long recallUserId = SecurityUserUtils.getUserId();
		if (baseMapper.recallTask(taskId, Instant.now(), recallUserId) <= 0) {
			// CAS 失败：并发下状态已变
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchDelete(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<InAppMessageEntity> tasks = baseMapper.selectByIds(ids);
		boolean blocked = tasks.stream().anyMatch(task -> {
			InAppMessageStatus status = InAppMessageStatus.from(task.getStatus());
			return status == null || !status.isDeletable();
		});
		if (blocked) {
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		baseMapper.deleteByIds(ids);
	}

}
