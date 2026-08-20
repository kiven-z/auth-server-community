package com.auth.service.auth.service;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.model.value.invalidation.InvalidationIdempotencyGate;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventStatsVO;

import java.time.Instant;

/**
 * 授权失效幂等事件：运维门禁与查询
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationEventService {

	/**
	 * 分页查询幂等事件
	 * @param query HTTP 查询条件
	 * @return 分页 VO
	 */
	PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query);

	/**
	 * 查询幂等事件详情
	 * @param id 主键
	 * @return 详情 VO
	 */
	AuthorizationInvalidationEventDetailVO getDetail(Long id);

	/**
	 * 查询幂等事件统计
	 * @return 统计 VO，无数据时各计数归零
	 */
	AuthorizationInvalidationEventStatsVO getStats();

	/**
	 * 查询已处理结果或抢占 eventId 处理权（INSERT 占位行，依赖 uk_event_id）
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @return 幂等门禁结果
	 */
	InvalidationIdempotencyGate acquireGate(String eventId, AuthorizationChangeKind kind);

	/**
	 * 将占位行更新为最终处理结果
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @param response 执行结果
	 */
	void completeProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response);

	/**
	 * 处理失败时释放占位，便于 Outbox 重试
	 * @param eventId 业务事件 ID
	 * @return 是否实际释放
	 */
	boolean releaseClaim(String eventId);

	/**
	 * 释放 processing 占位行，允许 Outbox 重新投递
	 * @param eventId 业务事件 ID
	 * @return 是否实际删除了占位行
	 */
	boolean releaseProcessingClaim(String eventId);

	/**
	 * 批量释放超时的 processing 占位行
	 * @param cutoffTime 占位 updated_at 早于此时间视为超时
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	int cleanupStaleProcessingClaims(Instant cutoffTime, int batchSize);

	/**
	 * 批量删除已完成的过期幂等事件
	 * @param cutoffTime processed_at 早于此时间的已完成行可被删除
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	int purgeCompletedBefore(Instant cutoffTime, int batchSize);

}
