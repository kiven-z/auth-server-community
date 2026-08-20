package com.auth.service.system.authorization.dispatch.query;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.feign.MeSessionInternalFeignClient;
import com.auth.service.system.authorization.feign.dto.UserSessionRemoteDTO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserSessionQueryOperations} 单元测试。
 */
@DisplayName("UserSessionQueryOperations 用户会话查询")
@ExtendWith(MockitoExtension.class)
class UserSessionQueryOperationsTest {

	@Mock
	private MeSessionInternalFeignClient meSessionInternalFeignClient;

	@InjectMocks
	private UserSessionQueryOperations userSessionQueryOperations;

	@Test
	@DisplayName("listActiveSessions：Feign 成功时映射快照")
	void listActiveSessions_whenFeignSuccess_mapsSnapshots() {
		UserSessionRemoteDTO remote = new UserSessionRemoteDTO();
		remote.setSessionId("jti-1");
		remote.setIpAddress("127.0.0.1");
		when(meSessionInternalFeignClient.listUserSessions(1L)).thenReturn(Result.success(List.of(remote)));

		List<UserSessionSnapshot> snapshots = userSessionQueryOperations.listActiveSessions(1L);

		assertThat(snapshots).hasSize(1);
		assertThat(snapshots.get(0).getSessionId()).isEqualTo("jti-1");
		assertThat(snapshots.get(0).getIpAddress()).isEqualTo("127.0.0.1");
	}

	@Test
	@DisplayName("listActiveSessions：Feign 失败时抛出 SERVICE_UNAVAILABLE")
	void listActiveSessions_whenFeignFails_throwsServiceUnavailable() {
		when(meSessionInternalFeignClient.listUserSessions(2L)).thenReturn(Result.error("unavailable"));

		assertThatThrownBy(() -> userSessionQueryOperations.listActiveSessions(2L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.SERVICE_UNAVAILABLE);
	}

	@Test
	@DisplayName("kickSession：委托 Feign 踢出会话")
	void kickSession_delegatesToFeign() {
		when(meSessionInternalFeignClient.kickUserSession(3L, "jti-2")).thenReturn(Result.success());

		userSessionQueryOperations.kickSession(3L, "jti-2");

		verify(meSessionInternalFeignClient).kickUserSession(3L, "jti-2");
	}

	@Test
	@DisplayName("kickSession：Feign 失败时抛出 OPERATION_FAILED")
	void kickSession_whenFeignFails_throwsOperationFailed() {
		when(meSessionInternalFeignClient.kickUserSession(4L, "jti-3")).thenReturn(Result.error("failed"));

		assertThatThrownBy(() -> userSessionQueryOperations.kickSession(4L, "jti-3"))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.OPERATION_FAILED);
	}

}
