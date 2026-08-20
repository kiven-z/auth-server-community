package com.auth.service.system.authorization.dispatch;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.outbox.AuthorizationInvalidationPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationCoordinator} 单元测试
 */
@DisplayName("AuthorizationInvalidationCoordinator 失效协调")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationCoordinatorTest {

	@Mock
	private AuthorizationInvalidationPublisher publisher;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private AuthorizationInvalidationProperties properties;

	@InjectMocks
	private AuthorizationInvalidationCoordinator coordinator;

	@Test
	@DisplayName("submit 写入 Outbox 并发布 AFTER_COMMIT 投递事件")
	void submit_shouldEnqueueAndPublishDispatchEvent() {
		when(properties.getSyncDispatchEnabled()).thenReturn(true);
		when(publisher.enqueue(org.mockito.ArgumentMatchers.any(), eq("SYS_ROLE"), eq("SUPER_ADMIN"))).thenReturn(100L);

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-1",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("SUPER_ADMIN")));
		coordinator.submit(request, "SYS_ROLE", "SUPER_ADMIN");

		verify(publisher).enqueue(request, "SYS_ROLE", "SUPER_ADMIN");
		ArgumentCaptor<AuthorizationInvalidationDispatchEvent> captor = ArgumentCaptor
			.forClass(AuthorizationInvalidationDispatchEvent.class);
		verify(applicationEventPublisher).publishEvent(captor.capture());
		assertEquals(100L, captor.getValue().outboxId());
	}

}
