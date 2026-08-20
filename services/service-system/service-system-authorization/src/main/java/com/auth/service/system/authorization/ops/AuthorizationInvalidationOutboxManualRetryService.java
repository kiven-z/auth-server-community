package com.auth.service.system.authorization.ops;

import com.auth.service.system.authorization.model.dto.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.model.form.AuthorizationInvalidationOutboxRetryForm;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxRetryResultVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Outbox 人工重试运维门面
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthorizationInvalidationOutboxManualRetryService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	/**
	 * 人工重试单条 Outbox
	 * @param outboxId Outbox 主键
	 * @param form 重试参数
	 * @return 重试结果摘要
	 */
	public AuthorizationInvalidationOutboxRetryResultVO retryById(Long outboxId,
			AuthorizationInvalidationOutboxRetryForm form) {
		boolean force = form != null && form.getForce() != null && form.getForce();

		AuthorizationInvalidationOutboxManualRetryOutcome outcome = authorizationInvalidationOutboxOpsService
			.retryManual(outboxId, force);

		AuthorizationInvalidationOutboxRetryResultVO vo = new AuthorizationInvalidationOutboxRetryResultVO();
		vo.setOutboxId(outcome.getOutboxId());
		vo.setEventId(outcome.getEventId());
		vo.setPreviousStatus(outcome.getPreviousStatus());
		vo.setCurrentStatus(outcome.getCurrentStatus());
		vo.setLastError(outcome.getLastError());
		vo.setDispatched(outcome.isDispatched());
		vo.setClaimReleased(outcome.isClaimReleased());
		return vo;
	}

}
