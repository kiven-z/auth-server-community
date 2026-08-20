package com.auth.service.system.authorization.dispatch.trigger;

import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link UserAuthorizationInvalidationTrigger} 单元测试
 */
@DisplayName("UserAuthorizationInvalidationTrigger 用户授权失效触发")
@ExtendWith(MockitoExtension.class)
class UserAuthorizationInvalidationTriggerTest {

	@Mock
	private AuthorizationInvalidationCoordinator invalidationCoordinator;

	@InjectMocks
	private UserAuthorizationInvalidationTrigger userInvalidationTrigger;

	@Test
	@DisplayName("空 userIds 时跳过提交")
	void submitByUserIds_whenEmpty_shouldSkip() {
		userInvalidationTrigger.submitByUserIds(Collections.emptyList(), "update");
		userInvalidationTrigger.submitByUserIds(Arrays.asList(null, null), "delete");

		verify(invalidationCoordinator, never()).submit(any(), any(), any());
	}

	@Test
	@DisplayName("按 userId 提交 USER 失效事件")
	void submitByUserIds_shouldSubmitUserInvalidation() {
		userInvalidationTrigger.submitByUserIds(Arrays.asList(10L, null, 10L, 20L), "update");

		ArgumentCaptor<AuthorizationInvalidateRequest> requestCaptor = ArgumentCaptor
			.forClass(AuthorizationInvalidateRequest.class);
		ArgumentCaptor<String> sourceBizIdCaptor = ArgumentCaptor.forClass(String.class);
		verify(invalidationCoordinator).submit(requestCaptor.capture(),
				org.mockito.ArgumentMatchers.eq(PlatformBizCodes.SYS_USER), sourceBizIdCaptor.capture());

		assertTrue(sourceBizIdCaptor.getValue().matches("update:[0-9a-f]{8}"));
		AuthorizationInvalidateRequest request = requestCaptor.getValue();
		assertEquals(AuthorizationChangeKind.USER, request.kind());
		assertInstanceOf(UserInvalidatePayload.class, request.payload());
		assertEquals(List.of(10L, 20L), ((UserInvalidatePayload) request.payload()).userIds());
	}

}
