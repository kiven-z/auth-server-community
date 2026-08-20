package com.auth.service.auth.job;

import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 授权失效幂等事件 processing 占位超时清理：删除孤儿占位行，允许 Outbox 重新投递
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationProcessingClaimCleanupJob {

	/**
	 * processing 占位超时清理每批上限（小于常规批处理档，控制单次事务）
	 */
	private static final int PROCESSING_CLAIM_CLEANUP_BATCH = 100;

	private final AuthorizationInvalidationEventService authorizationInvalidationEventService;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 定时扫描并释放超时的 processing 占位
	 */
	@Scheduled(fixedDelayString = "${auth.invalidation.processing-claim-cleanup-interval-ms:300000}")
	public void cleanupStaleProcessingClaims() {
		if (!properties.getProcessingClaimCleanupEnabled()) {
			return;
		}

		Instant cutoff = Instant.now().minus(properties.getProcessingClaimTimeoutMinutes(), ChronoUnit.MINUTES);
		int deleted = authorizationInvalidationEventService.cleanupStaleProcessingClaims(cutoff,
				PROCESSING_CLAIM_CLEANUP_BATCH);
		if (deleted > 0) {
			log.warn("Released stale invalidation processing claims: count={}, cutoff={}", deleted, cutoff);
		}
	}

}
