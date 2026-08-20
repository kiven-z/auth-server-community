package com.auth.service.system.message.service.me.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.message.convert.InAppInboxConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppInboxMapper;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.mapper.InAppMessageUserStatusMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageUserStatusEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.po.InAppInboxDetailRowPO;
import com.auth.service.system.message.model.po.InAppInboxMajorUnreadRowPO;
import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.auth.service.system.message.model.vo.inapp.InAppInboxDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxMajorUnreadVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxUnreadCountVO;
import com.auth.service.system.message.service.me.InAppInboxService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 用户侧站内信收件箱服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class InAppInboxServiceImpl implements InAppInboxService {

	private final InAppInboxMapper inAppInboxMapper;

	private final InAppMessageMapper inAppMessageMapper;

	private final InAppMessageRecipientMapper inAppMessageRecipientMapper;

	private final InAppMessageUserStatusMapper inAppMessageUserStatusMapper;

	/**
	 * 解析当前用户，去重后查主表，按投递模式拆成可操作的写扩散 / 读扩散 ID
	 * @param messageIds 站内信 ID 列表
	 * @return 分流结果；无可操作时两侧均为空列表
	 */
	private ActionableInboxIds resolveActionableIds(List<Long> messageIds) {
		Long userId = SecurityUserUtils.getUserId();
		if (CollUtil.isEmpty(messageIds)) {
			return new ActionableInboxIds(userId, Collections.emptyList(), Collections.emptyList());
		}

		List<Long> ids = messageIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return new ActionableInboxIds(userId, Collections.emptyList(), Collections.emptyList());
		}

		List<InAppMessageEntity> messages = inAppMessageMapper.selectList(Wrappers.<InAppMessageEntity>lambdaQuery()
			.select(InAppMessageEntity::getId, InAppMessageEntity::getRecipientScopeType, InAppMessageEntity::getStatus)
			.in(InAppMessageEntity::getId, ids));
		if (CollUtil.isEmpty(messages)) {
			return new ActionableInboxIds(userId, Collections.emptyList(), Collections.emptyList());
		}

		List<Long> pushIds = messages.stream().filter(entity -> {
			RecipientScopeType scopeType = RecipientScopeType.from(entity.getRecipientScopeType());
			return scopeType != null && !scopeType.isPull();
		}).filter(entity -> {
			InAppMessageStatus status = InAppMessageStatus.from(entity.getStatus());
			return status == InAppMessageStatus.SUCCESS || status == InAppMessageStatus.PARTIAL;
		}).map(InAppMessageEntity::getId).toList();

		List<Long> pullIds = messages.stream().filter(entity -> {
			RecipientScopeType scopeType = RecipientScopeType.from(entity.getRecipientScopeType());
			return scopeType != null && scopeType.isPull();
		})
			.filter(entity -> InAppMessageStatus.from(entity.getStatus()) == InAppMessageStatus.SUCCESS)
			.map(InAppMessageEntity::getId)
			.toList();

		return new ActionableInboxIds(userId, pushIds, pullIds);
	}

	/**
	 * 应用标记已读
	 * @param messageIds 消息 ID 列表
	 */
	private void applyMarkRead(List<Long> messageIds) {
		ActionableInboxIds actionable = resolveActionableIds(messageIds);
		if (actionable.isEmpty()) {
			return;
		}

		Long userId = actionable.userId();
		List<Long> pushIds = actionable.pushIds();
		List<Long> pullIds = actionable.pullIds();
		Instant readTime = Instant.now();
		if (CollUtil.isNotEmpty(pushIds)) {
			inAppMessageRecipientMapper.markRead(userId, pushIds, readTime);
		}
		if (CollUtil.isNotEmpty(pullIds)) {
			List<InAppMessageUserStatusEntity> rows = pullIds.stream().map(messageId -> {
				InAppMessageUserStatusEntity row = new InAppMessageUserStatusEntity();
				row.setId(IdWorker.getId());
				row.setMessageId(messageId);
				row.setUserId(userId);
				row.setIsRead(Boolean.TRUE);
				row.setReadTime(readTime);
				row.setIsDeleted(Boolean.FALSE);
				row.setCreatedBy(userId);
				row.setUpdatedBy(userId);
				row.setVersion(0L);
				return row;
			}).toList();
			inAppMessageUserStatusMapper.upsertReadBatch(rows);
		}
	}

	/**
	 * 应用批量删除
	 * @param messageIds 消息 ID 列表
	 */
	private void applyBatchDelete(List<Long> messageIds) {
		ActionableInboxIds actionable = resolveActionableIds(messageIds);
		if (actionable.isEmpty()) {
			return;
		}

		Long userId = actionable.userId();
		List<Long> pushIds = actionable.pushIds();
		List<Long> pullIds = actionable.pullIds();
		if (CollUtil.isNotEmpty(pushIds)) {
			inAppMessageRecipientMapper.markDeleted(userId, pushIds);
		}
		if (CollUtil.isNotEmpty(pullIds)) {
			List<InAppMessageUserStatusEntity> rows = pullIds.stream().map(messageId -> {
				InAppMessageUserStatusEntity row = new InAppMessageUserStatusEntity();
				row.setId(IdWorker.getId());
				row.setMessageId(messageId);
				row.setUserId(userId);
				row.setIsRead(Boolean.FALSE);
				row.setReadTime(null);
				row.setIsDeleted(Boolean.TRUE);
				row.setCreatedBy(userId);
				row.setUpdatedBy(userId);
				row.setVersion(0L);
				return row;
			}).toList();
			inAppMessageUserStatusMapper.upsertDeletedBatch(rows);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<InAppInboxPageVO> getInboxPage(InAppInboxQuery query) {
		Long userId = SecurityUserUtils.getUserId();

		Page<InAppInboxPageRowPO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<InAppInboxPageRowPO> page = inAppInboxMapper.selectInboxPage(pageParams, userId, query);

		IPage<InAppInboxPageVO> voPage = page.convert(InAppInboxConverter.INSTANCE::toPageVO);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public InAppInboxUnreadCountVO getUnreadCount() {
		Long userId = SecurityUserUtils.getUserId();

		List<InAppInboxMajorUnreadRowPO> rows = inAppInboxMapper.selectUnreadCountByMajor(userId);
		if (CollUtil.isEmpty(rows)) {
			return InAppInboxUnreadCountVO.builder().totalUnreadCount(0L).majors(Collections.emptyList()).build();
		}

		List<InAppInboxMajorUnreadVO> majors = rows.stream()
			.map(row -> InAppInboxMajorUnreadVO.builder()
				.majorCategoryId(row.getMajorCategoryId())
				.majorCategoryName(row.getMajorCategoryName())
				.majorCategoryCode(row.getMajorCategoryCode())
				.unreadCount(row.getUnreadCount())
				.build())
			.toList();
		long total = majors.stream().mapToLong(InAppInboxMajorUnreadVO::getUnreadCount).sum();
		return InAppInboxUnreadCountVO.builder().totalUnreadCount(total).majors(majors).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public InAppInboxDetailVO getInboxDetail(Long messageId) {
		Long userId = SecurityUserUtils.getUserId();
		InAppInboxDetailRowPO row = inAppInboxMapper.selectInboxDetail(userId, messageId);
		if (row == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		InAppInboxDetailVO vo = InAppInboxConverter.INSTANCE.toDetailVO(row);
		Instant readTime = Instant.now();
		applyMarkRead(List.of(messageId));
		vo.setIsRead(Boolean.TRUE);
		vo.setReadTime(readTime);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void markRead(List<Long> messageIds) {
		applyMarkRead(messageIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void markAllRead(Long majorCategoryId) {
		Long userId = SecurityUserUtils.getUserId();
		List<Long> messageIds = inAppInboxMapper.selectUnreadMessageIdsByMajor(userId, majorCategoryId);
		if (CollUtil.isEmpty(messageIds)) {
			return;
		}
		for (List<Long> batch : CollUtil.split(messageIds, BatchSizes.SIZE_500)) {
			applyMarkRead(batch);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchDelete(List<Long> messageIds) {
		applyBatchDelete(messageIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteAll(Long majorCategoryId) {
		Long userId = SecurityUserUtils.getUserId();
		List<Long> messageIds = inAppInboxMapper.selectVisibleMessageIdsByMajor(userId, majorCategoryId);
		if (CollUtil.isEmpty(messageIds)) {
			return;
		}
		for (List<Long> batch : CollUtil.split(messageIds, BatchSizes.SIZE_500)) {
			applyBatchDelete(batch);
		}
	}

	/**
	 * 写扩散 / 读扩散可操作消息 ID
	 *
	 * @param userId 当前用户 ID
	 * @param pushIds 写扩散消息 ID
	 * @param pullIds 读扩散消息 ID
	 */
	private record ActionableInboxIds(Long userId, List<Long> pushIds, List<Long> pullIds) {

		/**
		 * 是否无可操作消息
		 * @return true=两侧均空
		 */
		boolean isEmpty() {
			return CollUtil.isEmpty(pushIds) && CollUtil.isEmpty(pullIds);
		}

	}

}
