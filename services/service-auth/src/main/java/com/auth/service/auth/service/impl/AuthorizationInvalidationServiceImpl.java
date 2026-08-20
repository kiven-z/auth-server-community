package com.auth.service.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.model.command.invalidation.InvalidationCommand;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.model.value.invalidation.InvalidationIdempotencyGate;
import com.auth.service.auth.model.value.invalidation.UserInvalidationBuckets;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import com.auth.service.auth.service.AuthorizationInvalidationService;
import com.auth.service.auth.support.invalidation.AuthProfileMaterializationService;
import com.auth.service.auth.support.invalidation.UserInvalidationRepository;
import com.auth.service.auth.support.invalidation.impact.ImpactResolverRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 授权失效编排唯一入口实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorizationInvalidationServiceImpl implements AuthorizationInvalidationService {

	private final AuthorizationInvalidationEventService authorizationInvalidationEventService;

	private final ImpactResolverRegistry impactResolverRegistry;

	private final UserInvalidationRepository userInvalidationRepository;

	private final AuthProfileMaterializationService authProfileMaterializationService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidateResponse invalidate(AuthorizationInvalidateRequest request) {
		// 构建命令
		InvalidationCommand command = InvalidationCommand.builder()
			.eventId(request.eventId())
			.payload(request.payload())
			.build();
		String eventId = command.eventId();

		// 查询幂等门禁结果
		InvalidationIdempotencyGate gate = authorizationInvalidationEventService.acquireGate(eventId, request.kind());

		// 已处理完成，直接返回历史结果
		if (gate instanceof InvalidationIdempotencyGate.Completed completed) {
			log.debug("Authorization invalidate skipped (idempotent): eventId={}", eventId);
			return completed.response();
		}

		// 处理中，返回空结果
		if (gate instanceof InvalidationIdempotencyGate.InProgress) {
			log.warn("Authorization invalidate in progress by another worker: eventId={}", eventId);
			return AuthorizationInvalidateResponse.empty();
		}

		try {
			// 解析影响用户ID
			Set<Long> impactedUserIds = impactResolverRegistry.resolve(command.payload());

			// 无影响面时也需要更新数据库
			if (CollUtil.isEmpty(impactedUserIds)) {
				AuthorizationInvalidateResponse empty = AuthorizationInvalidateResponse.empty();
				authorizationInvalidationEventService.completeProcessedOutcome(eventId, request.kind(), empty);
				return empty;
			}

			// 查询用户状态快照
			List<UserInvalidationStatePO> invalidationStateList = userInvalidationRepository
				.loadByUserIds(impactedUserIds);
			// 有行：升版本并刷画像；无行：仅驱逐
			UserInvalidationBuckets buckets = partition(impactedUserIds, invalidationStateList);
			Set<Long> evictOnlyUserIds = buckets.evictOnlyUserIds();
			Set<Long> versionBumpUserIds = buckets.versionBumpUserIds();

			int versionBumpedCount = 0;
			int profileRefreshedCount = 0;
			if (CollUtil.isNotEmpty(versionBumpUserIds)) {
				versionBumpedCount = userInvalidationRepository.incrementPermVersionInBatches(versionBumpUserIds);
				profileRefreshedCount = authProfileMaterializationService.refreshInBatches(versionBumpUserIds);
			}

			int profileEvictedCount = 0;
			if (CollUtil.isNotEmpty(evictOnlyUserIds)) {
				profileEvictedCount = authProfileMaterializationService.evictInBatches(evictOnlyUserIds);
			}

			// 构建执行结果
			int impactedUserCount = impactedUserIds.size();
			AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(impactedUserCount,
					versionBumpedCount, profileRefreshedCount, profileEvictedCount);
			authorizationInvalidationEventService.completeProcessedOutcome(eventId, request.kind(), response);
			log.info(
					"Authorization invalidate completed: eventId={}, kind={}, impacted={}, bumped={}, refreshed={}, evicted={}",
					eventId, request.kind(), response.impactedUserCount(), response.versionBumpedCount(),
					response.profileRefreshedCount(), response.profileEvictedCount());
			return response;
		}
		catch (RuntimeException ex) {
			// 释放幂等锁
			authorizationInvalidationEventService.releaseClaim(eventId);
			throw ex;
		}
	}

	/**
	 * 按影响面与状态快照分桶：有用户行则递增版本并刷画像；库中已无行则仅驱逐缓存
	 * @param impactedUserIds 失效影响用户 ID
	 * @param states 用户状态快照（可与影响面部分重合）
	 * @return 分桶结果
	 */
	private UserInvalidationBuckets partition(Set<Long> impactedUserIds, List<UserInvalidationStatePO> states) {
		Set<Long> versionBumpUserIds = CollUtil.emptyIfNull(states)
			.stream()
			.map(UserInvalidationStatePO::getUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Set<Long> evictOnlyUserIds = CollUtil.emptyIfNull(impactedUserIds)
			.stream()
			.filter(Objects::nonNull)
			.filter(id -> !versionBumpUserIds.contains(id))
			.collect(Collectors.toSet());

		return UserInvalidationBuckets.builder()
			.versionBumpUserIds(versionBumpUserIds)
			.evictOnlyUserIds(evictOnlyUserIds)
			.build();
	}

}
