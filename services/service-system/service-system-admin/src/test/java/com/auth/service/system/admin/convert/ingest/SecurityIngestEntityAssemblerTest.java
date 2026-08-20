package com.auth.service.system.admin.convert.ingest;

import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SecurityIngestEntityAssembler} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SecurityIngestEntityAssembler 安全上报实体组装")
class SecurityIngestEntityAssemblerTest {

	@Test
	@DisplayName("操作日志：未登录时操作用户与审计列为 null")
	void toEntity_operationLog_nullPrincipal() {
		OperationLogPayloadEvent payload = OperationLogPayloadEvent.builder()
			.operationType("QUERY")
			.module("EXAMPLE:EXAMPLE_DEMO")
			.requestMethod("GET")
			.requestUri("/api/example")
			.ipAddress("127.0.0.1")
			.build();

		LogOperationEntity entity = SecurityIngestEntityAssembler.toEntity(payload, null);

		assertThat(entity.getUserId()).isNull();
		assertThat(entity.getCreatedBy()).isNull();
		assertThat(entity.getUpdatedBy()).isNull();
		assertThat(entity.getOperationType()).isEqualTo("QUERY");
	}

	@Test
	@DisplayName("操作日志：已登录时操作用户与审计列使用 principalUserId")
	void toEntity_operationLog_withPrincipal() {
		OperationLogPayloadEvent payload = OperationLogPayloadEvent.builder()
			.username("alice")
			.operationType("UPDATE")
			.module("SYSTEM:SYS_DEPT")
			.requestMethod("PUT")
			.requestUri("/api/system/dept/1")
			.ipAddress("10.0.0.1")
			.build();

		LogOperationEntity entity = SecurityIngestEntityAssembler.toEntity(payload, 100L);

		assertThat(entity.getUserId()).isEqualTo(100L);
		assertThat(entity.getUsername()).isEqualTo("alice");
		assertThat(entity.getCreatedBy()).isEqualTo(100L);
		assertThat(entity.getUpdatedBy()).isEqualTo(100L);
	}

	@Test
	@DisplayName("授权审计：映射事件字段并截断超长参数 JSON")
	void toEntity_authorizationAudit() {
		String longValue = "x".repeat(3000);
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(SecurityEventType.DENIED)
			.userId(42L)
			.requestIp("192.168.1.1")
			.requestMethod("POST")
			.requestUri("/api/system/role")
			.requiredAuthority("system:role:add")
			.decisionReason("missing permission")
			.exceptionMessage("x".repeat(3000))
			.className("com.example.RoleController")
			.methodName("create")
			.methodParams(Map.of("name", longValue))
			.build();

		LogAuthorizationAuditEntity entity = SecurityIngestEntityAssembler.toEntity(event);

		assertThat(entity.getEventType()).isEqualTo("DENIED");
		assertThat(entity.getCreatedBy()).isEqualTo(42L);
		assertThat(entity.getRequiredPermission()).isEqualTo("system:role:add");
		assertThat(entity.getRequestIp()).isEqualTo("192.168.1.1");
		assertThat(entity.getDecisionReason()).isEqualTo("missing permission");
		assertThat(entity.getExceptionMessage()).hasSize(2048);
		assertThat(entity.getMethodParams()).hasSize(2048);
	}

	@Test
	@DisplayName("授权审计：eventType 为空时默认 DENIED")
	void toEntity_authorizationAudit_defaultEventType() {
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.userId(null)
			.requestMethod("GET")
			.requestUri("/api/example")
			.build();

		LogAuthorizationAuditEntity entity = SecurityIngestEntityAssembler.toEntity(event);

		assertThat(entity.getEventType()).isEqualTo("DENIED");
		assertThat(entity.getCreatedBy()).isZero();
	}

}
