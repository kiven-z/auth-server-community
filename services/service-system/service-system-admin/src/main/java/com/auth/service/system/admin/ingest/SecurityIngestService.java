package com.auth.service.system.admin.ingest;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.service.system.admin.convert.ingest.SecurityIngestEntityAssembler;
import com.auth.service.system.admin.mapper.admin.log.LogAuthorizationAuditMapper;
import com.auth.service.system.admin.mapper.admin.log.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import org.springframework.stereotype.Service;

/**
 * 安全类内部上报数据落库服务
 *
 * @author Bunny
 */
@Service
public class SecurityIngestService {

	private final LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	private final LogOperationMapper logOperationMapper;

	public SecurityIngestService(LogAuthorizationAuditMapper logAuthorizationAuditMapper,
			LogOperationMapper logOperationMapper) {
		this.logAuthorizationAuditMapper = logAuthorizationAuditMapper;
		this.logOperationMapper = logOperationMapper;
	}

	/**
	 * 持久化一条授权审计记录
	 * @param event 授权审计负载
	 */
	public void append(SecurityAuthorizationAuditPayloadEvent event) {
		LogAuthorizationAuditEntity entity = SecurityIngestEntityAssembler.toEntity(event);
		logAuthorizationAuditMapper.insert(entity);
	}

	/**
	 * 持久化一条操作日志记录
	 * @param payload 操作日志负载
	 */
	public void append(OperationLogPayloadEvent payload) {
		Long principalUserId = payload.getUserId();
		LogOperationEntity entity = SecurityIngestEntityAssembler.toEntity(payload, principalUserId);
		logOperationMapper.insert(entity);
	}

}
