package com.auth.service.system.admin.ingest;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.system.admin.mapper.admin.log.LogAuthorizationAuditMapper;
import com.auth.service.system.admin.mapper.admin.log.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * {@link SecurityIngestService} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SecurityIngestService 安全上报落库")
@ExtendWith(MockitoExtension.class)
class SecurityIngestServiceTest {

	@Mock
	private LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	@Mock
	private LogOperationMapper logOperationMapper;

	private SecurityIngestService securityIngestService;

	@BeforeEach
	void setUp() {
		securityIngestService = new SecurityIngestService(logAuthorizationAuditMapper, logOperationMapper);
	}

	@Test
	@DisplayName("append(授权审计)：未认证主体时 created_by 落库为 0")
	void appendAuthorizationAuditUsesZeroWhenUserIdNull() {
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(SecurityEventType.GRANTED)
			.userId(null)
			.decisionReason("UNAUTHENTICATED")
			.build();

		securityIngestService.append(event);

		ArgumentCaptor<LogAuthorizationAuditEntity> captor = ArgumentCaptor.forClass(LogAuthorizationAuditEntity.class);
		verify(logAuthorizationAuditMapper).insert(captor.capture());
		assertThat(captor.getValue().getCreatedBy()).isZero();
	}

	@Test
	@DisplayName("append(授权审计)：已认证主体时 created_by 为事件中的 userId")
	void appendAuthorizationAuditUsesEventUserId() {
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(SecurityEventType.DENIED)
			.userId(42L)
			.decisionReason("ACCESS_DENIED")
			.build();

		securityIngestService.append(event);

		ArgumentCaptor<LogAuthorizationAuditEntity> captor = ArgumentCaptor.forClass(LogAuthorizationAuditEntity.class);
		verify(logAuthorizationAuditMapper).insert(captor.capture());
		assertThat(captor.getValue().getCreatedBy()).isEqualTo(42L);
	}

	@Test
	@DisplayName("append(操作日志)：未登录时 user_id 与审计列为 null")
	void appendOperationLogUsesNullWhenUserIdNull() {
		OperationLogPayloadEvent payload = OperationLogPayloadEvent.builder()
			.userId(null)
			.operationType("QUERY")
			.module("EXAMPLE:EXAMPLE_DEMO")
			.requestMethod("GET")
			.requestUri("/api/example")
			.ipAddress("127.0.0.1")
			.build();

		securityIngestService.append(payload);

		ArgumentCaptor<LogOperationEntity> captor = ArgumentCaptor.forClass(LogOperationEntity.class);
		verify(logOperationMapper).insert(captor.capture());
		LogOperationEntity saved = captor.getValue();
		assertThat(saved.getUserId()).isNull();
		assertThat(saved.getCreatedBy()).isNull();
		assertThat(saved.getUpdatedBy()).isNull();
	}

	@Test
	@DisplayName("append(操作日志)：已登录时 user_id 与审计列为事件 userId")
	void appendOperationLogUsesEventUserId() {
		OperationLogPayloadEvent payload = OperationLogPayloadEvent.builder()
			.userId(9L)
			.username("bob")
			.operationType("CREATE")
			.module("SYSTEM:SYS_USER")
			.requestMethod("POST")
			.requestUri("/api/system/user")
			.ipAddress("127.0.0.1")
			.build();

		securityIngestService.append(payload);

		ArgumentCaptor<LogOperationEntity> captor = ArgumentCaptor.forClass(LogOperationEntity.class);
		verify(logOperationMapper).insert(captor.capture());
		LogOperationEntity saved = captor.getValue();
		assertThat(saved.getUserId()).isEqualTo(9L);
		assertThat(saved.getUsername()).isEqualTo("bob");
		assertThat(saved.getCreatedBy()).isEqualTo(9L);
		assertThat(saved.getUpdatedBy()).isEqualTo(9L);
	}

}
