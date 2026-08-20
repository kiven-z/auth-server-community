package com.auth.service.system.authorization.service.impl;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.feign.SessionRevocationInternalFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link UserSessionRevocationServiceImpl} 单元测试
 */
@DisplayName("UserSessionRevocationServiceImpl 会话撤销")
@ExtendWith(MockitoExtension.class)
class UserSessionRevocationServiceImplTest {

	@Mock
	private SessionRevocationInternalFeignClient sessionRevocationInternalFeignClient;

	@InjectMocks
	private UserSessionRevocationServiceImpl userSessionRevocationService;

	@Test
	@DisplayName("空列表：不调用 Feign")
	void revokeAllSessionsIgnoresEmptyIds() {
		userSessionRevocationService.revokeAllSessions(List.of());

		verify(sessionRevocationInternalFeignClient, never()).kickAllSessions(anyList());
	}

	@Test
	@DisplayName("有用户 ID：调用内部踢会话接口")
	void revokeAllSessionsShouldCallFeign() {
		List<Long> userIds = List.of(2L, 3L);
		when(sessionRevocationInternalFeignClient.kickAllSessions(userIds)).thenReturn(Result.success());

		userSessionRevocationService.revokeAllSessions(userIds);

		verify(sessionRevocationInternalFeignClient).kickAllSessions(userIds);
	}

	@Test
	@DisplayName("Feign 抛异常：吞掉异常不向外传播")
	void revokeAllSessionsShouldSwallowFeignException() {
		List<Long> userIds = List.of(2L);
		when(sessionRevocationInternalFeignClient.kickAllSessions(userIds)).thenThrow(new RuntimeException("boom"));

		userSessionRevocationService.revokeAllSessions(userIds);

		verify(sessionRevocationInternalFeignClient).kickAllSessions(userIds);
	}

}
