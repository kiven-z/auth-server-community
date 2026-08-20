package com.auth.service.system.authorization.job;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationOutboxProcessor;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.model.entity.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.authorization.model.enums.AuthorizationInvalidationOutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 授权失效 Outbox 补偿任务：扫描 PENDING / FAILED 并重试投递 auth。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationOutboxJob {

	/**
	 * Outbox 补偿扫描每批抢占条数（小于常规批处理档，降低锁竞争）
	 */
	private static final int OUTBOX_POLL_BATCH = 50;

	private static final List<String> CLAIMABLE_STATUSES = List.of(AuthorizationInvalidationOutboxStatus.PENDING.name(),
			AuthorizationInvalidationOutboxStatus.FAILED.name());

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final AuthorizationInvalidationOutboxProcessor outboxProcessor;

	private final String instanceSuffix = Long.toHexString(System.nanoTime());

	@Value("${spring.application.name:service-system}")
	private String applicationName;

	/**
	 * 定时扫描并补偿投递失败的 Outbox 记录。
	 */
	@Scheduled(fixedDelayString = "${auth.authorization.poll-interval-ms:30000}")
	public void pollAndDispatch() {
		Instant now = Instant.now();

		List<SysAuthorizationInvalidationOutboxEntity> rows = outboxMapper.selectClaimable(CLAIMABLE_STATUSES, now,
				OUTBOX_POLL_BATCH);
		if (CollUtil.isEmpty(rows)) {
			return;
		}

		String lockedBy = applicationName + "-" + instanceSuffix;
		for (SysAuthorizationInvalidationOutboxEntity row : rows) {
			try {
				outboxProcessor.processClaimedRow(row, lockedBy);
			}
			catch (RuntimeException ex) {
				log.warn("Outbox compensation failed: outboxId={}, eventId={}, cause={}", row.getId(), row.getEventId(),
						ex.getMessage());
			}
		}
	}

}
