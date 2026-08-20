package com.auth.service.system.authorization.outbox;

import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.model.entity.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.authorization.model.enums.AuthorizationInvalidationOutboxStatus;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 授权失效 Outbox 写入
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class AuthorizationInvalidationPublisher {

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final InvalidationOutboxPayloadCodec payloadCodec;

	/**
	 * 在调用方事务内写入 Outbox（status=PENDING）
	 * @param request 失效请求
	 * @param sourceModule 触发模块，如 SYS_ROLE
	 * @param sourceBizId 追踪号，如 update:a3f2c1b0
	 * @return 新 Outbox 主键
	 */
	@Transactional(rollbackFor = Exception.class)
	public Long enqueue(AuthorizationInvalidateRequest request, String sourceModule, String sourceBizId) {
		Instant now = Instant.now();
		SysAuthorizationInvalidationOutboxEntity outboxEntity = new SysAuthorizationInvalidationOutboxEntity();
		outboxEntity.setId(IdWorker.getId());
		outboxEntity.setEventId(request.eventId());
		outboxEntity.setChangeKind(request.kind().name());
		outboxEntity.setPayload(payloadCodec.serialize(request.payload()));
		outboxEntity.setStatus(AuthorizationInvalidationOutboxStatus.PENDING.name());
		outboxEntity.setRetryCount(0);
		outboxEntity.setMaxRetry(5);
		outboxEntity.setNextRetryAt(now);
		outboxEntity.setSourceModule(sourceModule);
		outboxEntity.setSourceBizId(sourceBizId);
		outboxEntity.setCreatedAt(now);
		outboxEntity.setUpdatedAt(now);
		outboxEntity.setVersion(0L);
		outboxMapper.insertPending(outboxEntity);
		return outboxEntity.getId();
	}

}
