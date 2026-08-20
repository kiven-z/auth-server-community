package com.auth.service.auth.job;

import com.auth.common.core.constants.BatchSizes;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 授权失效幂等事件 SUCCESS 历史清理：删除已完成且超过保留期的记录
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationEventHistoryCleanupJob {

	private final AuthorizationInvalidationEventService authorizationInvalidationEventService;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 定时清理已完成的过期幂等事件
	 */
	@Scheduled(cron = "${auth.invalidation.success-cleanup-cron:0 0 4 * * ?}")
	public void purgeCompletedHistory() {
		if (!properties.getSuccessCleanupEnabled()) {
			return;
		}

		Instant cutoff = Instant.now().minus(properties.getSuccessRetentionDays(), ChronoUnit.DAYS);
		int totalDeleted = purgeInBatches(cutoff);
		if (totalDeleted > 0) {
			log.info("Purged completed invalidation events: count={}, cutoff={}", totalDeleted, cutoff);
		}
	}

	/**
	 * 分批删除，避免单次锁表过大
	 * @param cutoff processed_at 早于此时间的已完成行可被删除
	 * @return 累计删除行数
	 */
	private int purgeInBatches(Instant cutoff) {
		int totalDeleted = 0;
		int deleted;
		do {
			deleted = authorizationInvalidationEventService.purgeCompletedBefore(cutoff, BatchSizes.SIZE_500);
			totalDeleted += deleted;
		}
		while (deleted >= BatchSizes.SIZE_500);
		return totalDeleted;
	}

}
