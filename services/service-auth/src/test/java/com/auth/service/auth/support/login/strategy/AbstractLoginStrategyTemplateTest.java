package com.auth.service.auth.support.login.strategy;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBadCredentialsException;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.value.login.LoginAccount;
import com.auth.service.auth.model.value.login.LoginResult;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serial;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AbstractLoginStrategyTemplate} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AbstractLoginStrategyTemplateTest {

	@Mock
	private AuthProfileRepository authProfileRepository;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private LoginFailureRateLimiter loginFailureRateLimiter;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Test
	@DisplayName("模板策略：成功路径会前置检查锁定并构建身份与 profile")
	void authenticate_shouldBuildProfileAndResponse_whenSuccess() {
		LoginAccount account = LoginAccount.builder()
			.id(TestConstants.USER_ID)
			.username("u")
			.status(1)
			.encodedPassword("encoded")
			.permVersion(1L)
			.build();

		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.roles(List.of("R1"))
			.permissions(List.of("P1"))
			.permVersion(1L)
			.build();
		when(authProfileRepository.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		TestStrategy strategy = new TestStrategy(authProfileRepository, loginAuditService, loginFailureRateLimiter,
				account, null);

		LoginResult result = strategy.authenticate(new TestLoginCommand(), httpServletRequest);

		verify(loginFailureRateLimiter).assertNotLocked(TestConstants.USER_ID);
		verify(authProfileRepository).buildByUserId(TestConstants.USER_ID);
		assertNotNull(result);
		assertEquals(TestConstants.USER_ID, result.authProfile().getUserId());
		assertEquals(TestConstants.USER_ID, result.authenticatedUser().id());
		assertEquals("u", result.authenticatedUser().username());
		assertEquals(List.of("R1"), result.authProfile().getRoles());
		assertEquals(List.of("P1"), result.authProfile().getPermissions());
	}

	@Test
	@DisplayName("模板策略：凭证错误时记录失败次数、写审计并抛 USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldRecordFailureAndThrowBizException_whenBadCredentials() {
		LoginAccount account = LoginAccount.builder()
			.id(TestConstants.USER_ID)
			.username("u")
			.status(1)
			.encodedPassword("encoded")
			.permVersion(1L)
			.build();

		when(loginFailureRateLimiter.recordFailure(TestConstants.USER_ID)).thenReturn(3);

		TestStrategy strategy = new TestStrategy(authProfileRepository, loginAuditService, loginFailureRateLimiter,
				account, new AuthBadCredentialsException("bad"));
		TestLoginCommand command = new TestLoginCommand();

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginFailureRateLimiter).recordFailure(TestConstants.USER_ID);
		verify(loginAuditService).auditBadCredentialsWithRemaining(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(TestConstants.USER_ID), eq("u"), eq(3));
	}

	static class TestLoginCommand extends BaseLoginCommand {

		@Serial
		private static final long serialVersionUID = 1L;

		@Override
		public AuthLoginLogType loginLogType() {
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		public String principalForAudit() {
			return "test";
		}

	}

	static class TestStrategy extends AbstractLoginStrategyTemplate<TestLoginCommand> {

		private final LoginAccount accountToReturn;

		private final RuntimeException authenticateException;

		TestStrategy(AuthProfileRepository authProfileRepository, LoginAuditService loginAuditService,
				LoginFailureRateLimiter loginFailureRateLimiter, LoginAccount accountToReturn,
				RuntimeException authenticateException) {
			super(authProfileRepository, loginAuditService, loginFailureRateLimiter);
			this.accountToReturn = accountToReturn;
			this.authenticateException = authenticateException;
		}

		@Override
		protected AuthLoginLogType loginLogType() {
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		protected LoginAccount resolveAccount(TestLoginCommand command) {
			return accountToReturn;
		}

		@Override
		protected AuthLoginLogType doAuthenticate(TestLoginCommand command, LoginAccount account) {
			if (authenticateException != null) {
				throw authenticateException;
			}
			return AuthLoginLogType.LOGIN_PASSWORD;
		}

		@Override
		public Class<TestLoginCommand> supports() {
			return TestLoginCommand.class;
		}

	}

}
