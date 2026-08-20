package com.auth.service.auth.job;

import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationEventHistoryCleanupJob} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventHistoryCleanupJob SUCCESS 历史清理")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationEventHistoryCleanupJobTest {

	@Mock
	private AuthorizationInvalidationEventService authorizationInvalidationEventService;

	@Mock
	private AuthorizationInvalidationProperties properties;

	@InjectMocks
	private AuthorizationInvalidationEventHistoryCleanupJob historyCleanupJob;

	@Test
	@DisplayName("未启用时不执行清理")
	void purgeCompletedHistory_shouldSkipWhenDisabled() {
		when(properties.getSuccessCleanupEnabled()).thenReturn(false);

		historyCleanupJob.purgeCompletedHistory();

		verify(authorizationInvalidationEventService, never()).purgeCompletedBefore(any(), anyInt());
	}

	@Test
	@DisplayName("启用时循环批量删除直到不足一批")
	void purgeCompletedHistory_shouldPurgeInBatchesWhenEnabled() {
		when(properties.getSuccessCleanupEnabled()).thenReturn(true);
		when(properties.getSuccessRetentionDays()).thenReturn(90);
		when(authorizationInvalidationEventService.purgeCompletedBefore(any(), anyInt())).thenReturn(500, 120);

		historyCleanupJob.purgeCompletedHistory();

		verify(authorizationInvalidationEventService, times(2)).purgeCompletedBefore(any(), anyInt());
	}

}
