package com.auth.service.system.authorization.service.impl;

import com.auth.service.system.authorization.mapper.AuthorizationInvalidationOutboxOpsMapper;
import com.auth.service.system.authorization.ops.AuthorizationInvalidationOutboxManualRetryApplicationService;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationOutboxOpsServiceImpl} 单元测试
 */
@DisplayName("AuthorizationInvalidationOutboxOpsServiceImpl 运维门面")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationOutboxOpsServiceImplTest {

	@Mock
	private AuthorizationInvalidationOutboxOpsMapper authorizationInvalidationOutboxOpsMapper;

	@Mock
	private AuthorizationInvalidationOutboxManualRetryApplicationService manualRetryApplicationService;

	private AuthorizationInvalidationOutboxOpsServiceImpl opsService;

	@BeforeEach
	void setUpService() {
		opsService = new AuthorizationInvalidationOutboxOpsServiceImpl(authorizationInvalidationOutboxOpsMapper,
				manualRetryApplicationService);
	}

	@Test
	@DisplayName("getDetail 记录不存在时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenMissing() {
		when(authorizationInvalidationOutboxOpsMapper.selectDetailById(99L)).thenReturn(null);

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> opsService.getDetail(99L));

		assertSame(DATA_NOT_EXIST, exception.getResultCode());
	}

}
