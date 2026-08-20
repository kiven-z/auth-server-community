package com.auth.service.system.authorization.ops;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationOutboxProcessor;
import com.auth.service.system.authorization.feign.AuthorizationInternalFeignClient;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.model.dto.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.model.entity.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.authorization.model.enums.AuthorizationInvalidationOutboxStatus;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static com.auth.service.system.authorization.exception.AuthorizationInvalidationOpsResultCode.*;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * Outbox 人工重试用例编排
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AuthorizationInvalidationOutboxManualRetryApplicationService {

	private static final Set<String> RESET_REQUIRED_STATUSES = Set.of(AuthorizationInvalidationOutboxStatus.DEAD.name(),
			AuthorizationInvalidationOutboxStatus.FAILED.name(),
			AuthorizationInvalidationOutboxStatus.PROCESSING.name());

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final AuthorizationInvalidationOutboxProcessor outboxProcessor;

	private final AuthorizationInternalFeignClient authorizationInternalFeignClient;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 人工重试单条 Outbox
	 * @param outboxId Outbox 主键
	 * @param force 是否强制重试 PROCESSING 占位
	 * @return 重试结果摘要
	 */
	public AuthorizationInvalidationOutboxManualRetryOutcome retryManual(Long outboxId, boolean force) {
		SysAuthorizationInvalidationOutboxEntity row = outboxMapper.selectById(outboxId);
		if (row == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		String previousStatus = row.getStatus();
		validateRetryable(row, force);

		boolean claimReleased = releaseEventClaimIfPresent(row.getEventId());
		if (RESET_REQUIRED_STATUSES.contains(previousStatus)) {
			resetForManualRetry(row);
		}

		boolean dispatched = outboxProcessor.processById(outboxId);
		SysAuthorizationInvalidationOutboxEntity latest = outboxMapper.selectById(outboxId);
		String currentStatus = latest != null ? latest.getStatus() : null;
		String lastError = latest != null ? latest.getLastError() : null;

		AuthorizationInvalidationOutboxManualRetryOutcome outcome = new AuthorizationInvalidationOutboxManualRetryOutcome();
		outcome.setOutboxId(outboxId);
		outcome.setEventId(row.getEventId());
		outcome.setPreviousStatus(previousStatus);
		outcome.setCurrentStatus(currentStatus);
		outcome.setLastError(lastError);
		outcome.setDispatched(dispatched);
		outcome.setClaimReleased(claimReleased);
		return outcome;
	}

	/**
	 * 验证 Outbox 是否可重试
	 * @param row Outbox 行
	 * @param force 是否强制重试
	 */
	private void validateRetryable(SysAuthorizationInvalidationOutboxEntity row, boolean force) {
		String status = row.getStatus();
		if (AuthorizationInvalidationOutboxStatus.SUCCESS.name().equals(status)) {
			throw new SystemBusinessException(RETRY_NOT_ALLOWED, status);
		}
		if (!AuthorizationInvalidationOutboxStatus.PROCESSING.name().equals(status)) {
			return;
		}
		if (force) {
			return;
		}
		if (isProcessingUnlockAllowed(row.getLockedAt())) {
			return;
		}
		throw new SystemBusinessException(PROCESSING_LOCKED);
	}

	/**
	 * 验证 PROCESSING 占位是否可解锁
	 * @param lockedAt 锁定时间
	 * @return 是否可解锁
	 */
	private boolean isProcessingUnlockAllowed(Instant lockedAt) {
		if (lockedAt == null) {
			return true;
		}
		long timeoutMinutes = properties.getManualProcessingUnlockTimeoutMinutes();
		return !lockedAt.plus(timeoutMinutes, ChronoUnit.MINUTES).isAfter(Instant.now());
	}

	/**
	 * 重置 Outbox 状态为 PROCESSING
	 * @param row Outbox 行
	 */
	private void resetForManualRetry(SysAuthorizationInvalidationOutboxEntity row) {
		int updated = outboxMapper.resetForManualRetry(row.getId(), row.getVersion());
		if (updated == 0) {
			throw new SystemBusinessException(RETRY_CONFLICT);
		}
	}

	/**
	 * 释放幂等事件处理中占位
	 * @param eventId 幂等事件 ID
	 * @return 是否释放成功
	 */
	private boolean releaseEventClaimIfPresent(String eventId) {
		Result<Boolean> result = authorizationInternalFeignClient.releaseInvalidationEventClaim(eventId);
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Release invalidation event claim unavailable, eventId={}", eventId);
			return false;
		}
		return result.getData() != null && result.getData();
	}

}
