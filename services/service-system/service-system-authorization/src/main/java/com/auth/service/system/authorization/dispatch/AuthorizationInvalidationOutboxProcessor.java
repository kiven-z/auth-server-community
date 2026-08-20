package com.auth.service.system.authorization.dispatch;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidatePayload;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.feign.AuthorizationInternalFeignClient;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.model.entity.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.authorization.model.enums.AuthorizationInvalidationOutboxStatus;
import com.auth.service.system.authorization.outbox.InvalidationOutboxPayloadCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

/**
 * Outbox 投递执行：调用 auth /invalidate 并更新 Outbox 状态。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorizationInvalidationOutboxProcessor {

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final InvalidationOutboxPayloadCodec payloadCodec;

	private final AuthorizationInternalFeignClient authorizationInternalFeignClient;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 处理单条 Outbox（同步或补偿 Job 共用）。
	 * @param outboxId Outbox 主键
	 * @return 是否处理成功
	 */
	public boolean processById(Long outboxId) {
		SysAuthorizationInvalidationOutboxEntity row = outboxMapper.selectById(outboxId);
		if (row == null) {
			log.warn("Outbox row not found, id={}", outboxId);
			return false;
		}
		if (AuthorizationInvalidationOutboxStatus.SUCCESS.name().equals(row.getStatus())) {
			return true;
		}
		return dispatchRow(row);
	}

	/**
	 * 补偿 Job：抢占后处理。
	 * @param row 已查询的 Outbox 行
	 * @param lockedBy 抢占实例标识
	 * @return 是否处理成功
	 */
	public boolean processClaimedRow(SysAuthorizationInvalidationOutboxEntity row, String lockedBy) {
		Instant now = Instant.now();
		int claimed = outboxMapper.claimForProcessing(row.getId(), lockedBy, now, row.getVersion());
		if (claimed == 0) {
			return false;
		}
		row.setStatus(AuthorizationInvalidationOutboxStatus.PROCESSING.name());
		row.setLockedBy(lockedBy);
		row.setLockedAt(now);
		row.setVersion(row.getVersion() + 1);
		return dispatchRow(row);
	}

	/**
	 * 处理 Outbox 行。
	 * @param row 已查询的 Outbox 行
	 * @return 是否处理成功
	 */
	private boolean dispatchRow(SysAuthorizationInvalidationOutboxEntity row) {
		String changeKind = row.getChangeKind();
		AuthorizationChangeKind kind = AuthorizationChangeKind.valueOf(changeKind);
		AuthorizationInvalidatePayload payload = payloadCodec.deserialize(kind, row.getPayload());
		var request = new AuthorizationInvalidateRequest(row.getEventId(), kind, payload);

		try {
			Result<AuthorizationInvalidateResponse> result = authorizationInternalFeignClient.invalidate(request);
			if (result != null && Integer.valueOf(Result.SUCCESS_CODE).equals(result.getCode())) {
				outboxMapper.markSuccess(row.getId(), Instant.now());
				log.info("Authorization invalidation dispatched: eventId={}, outboxId={}", row.getEventId(),
						row.getId());
				return true;
			}
			String message = result != null ? result.getMessage() : "null Result";
			handleFailure(row, "auth invalidate failed: " + message);
			return false;
		}
		catch (RuntimeException ex) {
			handleFailure(row, ex.getMessage());
			log.warn("Authorization invalidation dispatch error: eventId={}, outboxId={}, cause={}", row.getEventId(),
					row.getId(), ex.getMessage());
			return false;
		}
	}

	/**
	 * 处理失败情况。
	 * @param row 已查询的 Outbox 行
	 * @param errorMessage 错误消息
	 */
	private void handleFailure(SysAuthorizationInvalidationOutboxEntity row, String errorMessage) {
		String truncated = CharSequenceUtil.subPre(CharSequenceUtil.blankToDefault(errorMessage, "unknown error"),
				1900);
		if (AuthorizationInvalidationOutboxStatus.PENDING.name().equals(row.getStatus())) {
			outboxMapper.recordSyncFailure(row.getId(), truncated);
			return;
		}
		int nextRetry = row.getRetryCount() + 1;
		int maxRetry = Objects.requireNonNullElse(row.getMaxRetry(), 5);
		if (nextRetry >= maxRetry) {
			outboxMapper.markFailure(row.getId(), AuthorizationInvalidationOutboxStatus.DEAD.name(), nextRetry, null,
					truncated);
			return;
		}
		long backoffSeconds = (long) properties.getRetryBackoffSeconds() * (1L << Math.min(nextRetry, 10));
		Instant nextRetryAt = Instant.now().plusSeconds(backoffSeconds);
		outboxMapper.markFailure(row.getId(), AuthorizationInvalidationOutboxStatus.FAILED.name(), nextRetry,
				nextRetryAt, truncated);
	}

}
