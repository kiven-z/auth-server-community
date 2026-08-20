package com.auth.service.auth.support.login;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.value.login.AuthenticatedUser;
import com.auth.service.auth.model.value.login.CompletedLoginSession;
import com.auth.service.auth.model.value.login.LoginResult;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import com.auth.service.auth.model.value.login.command.UsernamePasswordCommand;
import com.auth.service.auth.support.login.strategy.LoginStrategy;
import com.auth.service.auth.support.session.UserSessionRedisStore;
import com.auth.service.auth.support.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link LoginService} 单元测试：策略路由、会话签发委托与登出。
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

	@Mock
	private LoginSessionOrchestrator loginSessionOrchestrator;

	@Mock
	private UserSessionRedisStore userSessionRedisStore;

	@Mock
	private TokenService tokenService;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private HttpServletRequest httpServletRequest;

	private static LoginResult toLoginResult(AuthProfile profile) {
		AuthenticatedUser user = AuthenticatedUser.builder().id(TestConstants.USER_ID).username("user").build();
		return LoginResult.builder()
			.authenticatedUser(user)
			.authProfile(profile)
			.loginLogType(AuthLoginLogType.LOGIN_PASSWORD)
			.build();
	}

	private LoginService newService(LoginStrategy<?>... strategies) {
		return new LoginService(List.of(strategies), loginSessionOrchestrator, userSessionRedisStore, tokenService,
				loginAuditService);
	}

	@Test
	@DisplayName("登录：策略认证成功后委托 LoginSessionOrchestrator 签发会话")
	void login_shouldDelegateToSessionOrchestrator_whenStrategyAuthenticates() {
		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("user");
		command.setPassword("pass");
		command.setRememberMe(true);

		AuthProfile profile = mock(AuthProfile.class);
		LoginResult loginResult = toLoginResult(profile);

		LoginStrategy<UsernamePasswordCommand> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordCommand> supports() {
				return UsernamePasswordCommand.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordCommand cmd, HttpServletRequest request) {
				return loginResult;
			}
		};

		CompletedLoginSession issued = new CompletedLoginSession();
		issued.setAccessToken(TestConstants.ACCESS_TOKEN);
		when(loginSessionOrchestrator.issueSession(eq(loginResult), eq(true), same(httpServletRequest)))
			.thenReturn(issued);

		CompletedLoginSession actual = newService(strategy).login(command, httpServletRequest);

		assertSame(issued, actual);
		verify(loginSessionOrchestrator).issueSession(eq(loginResult), eq(true), same(httpServletRequest));
	}

	@Test
	@DisplayName("登录：无精确匹配时，允许父类命令类型匹配兜底")
	void login_shouldFallbackToAssignableStrategy_whenExactClassNotFound() {
		// 子类命令应由父类策略处理
		class SubUsernamePasswordCommand extends UsernamePasswordCommand {

			SubUsernamePasswordCommand() {
				setUsername("user");
				setPassword("pass");
			}

		}

		SubUsernamePasswordCommand command = new SubUsernamePasswordCommand();
		AuthProfile profile = mock(AuthProfile.class);
		LoginResult loginResult = toLoginResult(profile);

		LoginStrategy<UsernamePasswordCommand> strategy = new LoginStrategy<>() {
			@Override
			public Class<UsernamePasswordCommand> supports() {
				return UsernamePasswordCommand.class;
			}

			@Override
			public LoginResult authenticate(UsernamePasswordCommand cmd, HttpServletRequest request) {
				return loginResult;
			}
		};

		CompletedLoginSession session = new CompletedLoginSession();
		session.setId(TestConstants.USER_ID);
		when(loginSessionOrchestrator.issueSession(eq(loginResult), eq(false), same(httpServletRequest)))
			.thenReturn(session);

		CompletedLoginSession actual = newService(strategy).login(command, httpServletRequest);

		assertEquals(TestConstants.USER_ID, actual.getId());
		verify(loginSessionOrchestrator).issueSession(eq(loginResult), eq(false), same(httpServletRequest));
	}

	@Test
	@DisplayName("登录：无任何策略时，抛出 AUTH_TYPE_UNSUPPORTED 并写审计")
	void login_shouldThrowAuthTypeUnsupported_whenNoStrategy() {
		// 匿名命令，无任何策略可匹配
		BaseLoginCommand unknown = new BaseLoginCommand() {
			@Override
			public AuthLoginLogType loginLogType() {
				return AuthLoginLogType.LOGIN_PASSWORD;
			}

			@Override
			public String principalForAudit() {
				return null;
			}
		};

		LoginService service = newService();
		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> service.login(unknown, httpServletRequest));

		assertEquals(AuthResultCode.AUTH_TYPE_UNSUPPORTED, exception.getResultCode());
		verify(loginAuditService).auditUnsupportedLoginCommand(same(httpServletRequest), eq(unknown),
				eq(exception.getMessageKey()));
		verifyNoInteractions(loginSessionOrchestrator);
	}

	@Test
	@DisplayName("登出：token 为空或无法解析时不删除会话")
	void logout_shouldSkipRevoke_whenTokenBlankOrUnparsed() {
		LoginService service = newService();

		service.logout(" ");
		verify(tokenService).parseAccessTokenSafe(" ");
		verifyNoInteractions(userSessionRedisStore);

		reset(tokenService, userSessionRedisStore);
		when(tokenService.parseAccessTokenSafe("x")).thenReturn(Optional.empty());
		service.logout("x");
		verify(tokenService).parseAccessTokenSafe("x");
		verifyNoInteractions(userSessionRedisStore);
	}

	@Test
	@DisplayName("登出：token 有效时，删除会话并从活跃集合移除")
	void logout_shouldDeleteSessionAndRemoveActiveSession_whenTokenValid() {
		LoginService service = newService();

		JwtUserToken jwtUserToken = mock(JwtUserToken.class);
		when(jwtUserToken.getJti()).thenReturn(TestConstants.JTI);
		when(jwtUserToken.getUserId()).thenReturn(TestConstants.USER_ID);

		when(tokenService.parseAccessTokenSafe(TestConstants.ACCESS_TOKEN)).thenReturn(Optional.of(jwtUserToken));

		var hint = service.logout(TestConstants.ACCESS_TOKEN);

		verify(userSessionRedisStore).terminateSession(TestConstants.USER_ID, TestConstants.JTI);
		verifyNoInteractions(loginAuditService);
		assertTrue(hint.isPresent());
		assertEquals(TestConstants.USER_ID, hint.get().userId());
		assertEquals(TestConstants.JTI, hint.get().jti());
	}

}
