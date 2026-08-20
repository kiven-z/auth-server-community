package com.auth.service.system.authorization.job;

import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
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
 * {@link AuthorizationInvalidationOutboxHistoryCleanupJob} 单元测试
 */
@DisplayName("AuthorizationInvalidationOutboxHistoryCleanupJob SUCCESS 历史清理")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationOutboxHistoryCleanupJobTest {

	@Mock
	private SysAuthorizationInvalidationOutboxMapper outboxMapper;

	@Mock
	private AuthorizationInvalidationProperties properties;

	@InjectMocks
	private AuthorizationInvalidationOutboxHistoryCleanupJob historyCleanupJob;

	@Test
	@DisplayName("未启用时不执行清理")
	void purgeSuccessHistory_shouldSkipWhenDisabled() {
		when(properties.getSuccessCleanupEnabled()).thenReturn(false);

		historyCleanupJob.purgeSuccessHistory();

		verify(outboxMapper, never()).deleteSuccessBefore(any(), anyInt());
	}

	@Test
	@DisplayName("启用时循环批量删除直到不足一批")
	void purgeSuccessHistory_shouldPurgeInBatchesWhenEnabled() {
		when(properties.getSuccessCleanupEnabled()).thenReturn(true);
		when(properties.getSuccessRetentionDays()).thenReturn(90);
		when(outboxMapper.deleteSuccessBefore(any(), anyInt())).thenReturn(500, 80);

		historyCleanupJob.purgeSuccessHistory();

		verify(outboxMapper, times(2)).deleteSuccessBefore(any(), anyInt());
	}

}
