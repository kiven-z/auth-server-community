package com.auth.service.auth.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.convert.AuthorizationInvalidationEventConverter;
import com.auth.service.auth.convert.AuthorizationInvalidationEventMappingSupport;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPO;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.model.value.invalidation.InvalidationIdempotencyGate;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import com.auth.service.auth.support.invalidation.InvalidationProcessingMarker;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * 授权失效幂等事件实现：运维门禁与查询
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthorizationInvalidationEventServiceImpl implements AuthorizationInvalidationEventService {

	private final AuthorizationInvalidationEventMapper invalidationEventMapper;

	/**
	 * 分页查询幂等事件
	 * @param query HTTP 查询条件
	 * @return 分页 VO
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query) {
		Page<AuthorizationInvalidationEventPageRowPO> pageParams = new Page<>(query.getPageIndex(),
				query.getPageSize());
		IPage<AuthorizationInvalidationEventPageRowPO> page = invalidationEventMapper.selectListByPage(pageParams,
				query);

		IPage<AuthorizationInvalidationEventPageVO> voPage = page
			.convert(AuthorizationInvalidationEventConverter.INSTANCE::toPageVO);
		return PageResponse.of(voPage);
	}

	/**
	 * 查询幂等事件详情
	 * @param id 主键
	 * @return 详情 VO
	 */
	@Override
	@Transactional(readOnly = true)
	public AuthorizationInvalidationEventDetailVO getDetail(Long id) {
		AuthorizationInvalidationEventPageRowPO row = invalidationEventMapper.selectDetailById(id);
		if (row == null) {
			throw new AuthBusinessException(AuthResultCode.DATA_NOT_EXIST);
		}

		return AuthorizationInvalidationEventConverter.INSTANCE.toDetailVo(row);
	}

	/**
	 * 查询幂等事件统计
	 * @return 统计 VO，无数据时各计数归零
	 */
	@Override
	@Transactional(readOnly = true)
	public AuthorizationInvalidationEventStatsVO getStats() {
		AuthorizationInvalidationEventStatsPO statsPo = invalidationEventMapper.selectEventStats();
		AuthorizationInvalidationEventStatsPO defaultObj = new AuthorizationInvalidationEventStatsPO();
		AuthorizationInvalidationEventStatsPO safePo = Objects.requireNonNullElse(statsPo, defaultObj);

		AuthorizationInvalidationEventStatsVO statsVo = new AuthorizationInvalidationEventStatsVO();
		statsVo.setTotalCount(Objects.requireNonNullElse(safePo.getTotalCount(), 0L));
		statsVo.setProcessingCount(Objects.requireNonNullElse(safePo.getProcessingCount(), 0L));
		statsVo.setCompletedCount(Objects.requireNonNullElse(safePo.getCompletedCount(), 0L));
		return statsVo;
	}

	/**
	 * 查询已处理结果或抢占
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @return 幂等门禁结果
	 */
	@Override
	public InvalidationIdempotencyGate acquireGate(String eventId, AuthorizationChangeKind kind) {
		AuthorizationInvalidationEventPO projection = invalidationEventMapper.selectByEventId(eventId);
		if (projection != null && !InvalidationProcessingMarker.isProcessing(projection.getImpactedUserCount())) {
			return InvalidationIdempotencyGate.Completed.builder()
				.response(AuthorizationInvalidationEventMappingSupport.toResponse(projection))
				.build();
		}

		try {
			invalidationEventMapper.insertProcessingClaim(
					AuthorizationInvalidationEventMappingSupport.buildProcessingClaim(eventId, kind));

			return InvalidationIdempotencyGate.Claimed.builder().build();
		}
		catch (DuplicateKeyException ex) {
			AuthorizationInvalidationEventPO existing = invalidationEventMapper.selectByEventId(eventId);

			if (existing == null || InvalidationProcessingMarker.isProcessing(existing.getImpactedUserCount())) {
				return InvalidationIdempotencyGate.InProgress.builder().build();
			}

			AuthorizationInvalidateResponse response = AuthorizationInvalidationEventMappingSupport
				.toResponse(existing);
			return InvalidationIdempotencyGate.Completed.builder().response(response).build();
		}
	}

	/**
	 * 将占位行更新为最终处理结果
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @param response 执行结果
	 */
	@Override
	public void completeProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response) {
		AuthorizationInvalidationEventPO projection = AuthorizationInvalidationEventMappingSupport
			.toProcessedOutcomeProjection(eventId, kind, response);

		int updated = invalidationEventMapper.updateProcessedOutcome(projection);
		if (updated == 0) {
			throw new IllegalStateException(
					"Failed to complete invalidation idempotency record, eventId=" + eventId + " (claim missing?)");
		}
	}

	/**
	 * 处理失败时释放占位，便于 Outbox 重试
	 * @param eventId 业务事件 ID
	 * @return 是否实际释放
	 */
	@Override
	public boolean releaseClaim(String eventId) {
		return invalidationEventMapper.deleteProcessingClaim(eventId) > 0;
	}

	/**
	 * 释放 processing 占位行，允许 Outbox 重新投递
	 * @param eventId 业务事件 ID
	 * @return 是否实际删除了占位行
	 */
	@Override
	public boolean releaseProcessingClaim(String eventId) {
		AuthorizationInvalidationEventPO projection = invalidationEventMapper.selectByEventId(eventId);
		Integer impactedUserCount = projection != null ? projection.getImpactedUserCount() : null;
		if (!InvalidationProcessingMarker.isProcessing(impactedUserCount)) {
			return false;
		}

		return invalidationEventMapper.deleteProcessingClaim(eventId) > 0;
	}

	/**
	 * 批量释放超时的 processing 占位行
	 * @param cutoffTime 占位 updated_at 早于此时间视为超时
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int cleanupStaleProcessingClaims(Instant cutoffTime, int batchSize) {
		return invalidationEventMapper.deleteStaleProcessingClaims(cutoffTime, batchSize);
	}

	/**
	 * 批量删除已完成的过期幂等事件
	 * @param cutoffTime processed_at 早于此时间的已完成行可被删除
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int purgeCompletedBefore(Instant cutoffTime, int batchSize) {
		return invalidationEventMapper.deleteCompletedBefore(cutoffTime, batchSize);
	}

}
