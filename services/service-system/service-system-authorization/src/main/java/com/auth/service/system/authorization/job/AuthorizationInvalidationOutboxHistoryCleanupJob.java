package com.auth.service.system.authorization.job;

import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 授权失效 Outbox SUCCESS 历史清理：删除已完成且超过保留期的投递记录
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationOutboxHistoryCleanupJob {

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 定时清理 SUCCESS 且已过保留期的 Outbox 记录
	 */
	@Scheduled(cron = "${auth.authorization.success-cleanup-cron:0 30 3 * * ?}")
	public void purgeSuccessHistory() {
		if (properties.getSuccessCleanupEnabled() == null || !properties.getSuccessCleanupEnabled()) {
			return;
		}

		Instant cutoff = Instant.now().minus(properties.getSuccessRetentionDays(), ChronoUnit.DAYS);
		int totalDeleted = purgeInBatches(cutoff);
		if (totalDeleted > 0) {
			log.info("Purged SUCCESS invalidation outbox rows: count={}, cutoff={}", totalDeleted, cutoff);
		}
	}

	/**
	 * 分批删除，避免单次锁表过大
	 * @param cutoff processed_at 早于此时间可被删除
	 * @return 累计删除行数
	 */
	private int purgeInBatches(Instant cutoff) {
		int totalDeleted = 0;
		while (true) {
			int deleted = outboxMapper.deleteSuccessBefore(cutoff, BatchSizes.SIZE_500);
			totalDeleted += deleted;
			if (deleted < BatchSizes.SIZE_500) {
				break;
			}
		}
		return totalDeleted;
	}

}
