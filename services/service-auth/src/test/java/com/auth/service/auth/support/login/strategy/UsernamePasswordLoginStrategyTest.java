package com.auth.service.auth.support.login.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.model.value.login.command.UsernamePasswordCommand;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * {@link UsernamePasswordLoginStrategy} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class UsernamePasswordLoginStrategyTest {

	@Mock
	private AuthProfileRepository authProfileRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private LoginFailureRateLimiter loginFailureRateLimiter;

	@Mock
	private UserMapper userMapper;

	@Mock
	private HttpServletRequest httpServletRequest;

	private UsernamePasswordLoginStrategy createStrategy() {
		return new UsernamePasswordLoginStrategy(authProfileRepository, passwordEncoder, loginAuditService,
				loginFailureRateLimiter, userMapper);
	}

	@Test
	@DisplayName("用户不存在时抛出 USERNAME_OR_PASSWORD_ERROR 并记录审计")
	void resolveUser_shouldThrow_whenUserNotFound() {
		when(userMapper.selectByCredential(CredentialDimension.USERNAME, "u", false)).thenReturn(null);
		UsernamePasswordLoginStrategy strategy = createStrategy();

		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("u");
		command.setPassword("p");
		when(loginAuditService.resolvePrincipal(command)).thenReturn("u");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditService).resolvePrincipal(command);
		verify(loginAuditService).auditUserUnknownAfterResolve(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq("u"), eq(exception.getMessageKey()));
	}

	@Test
	@DisplayName("账号禁用时抛出 USER_DISABLED 并记录审计")
	void authenticate_shouldThrowUserDisabled_whenAccountDisabled() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUsername("u");
		userEntity.setStatus(0);
		userEntity.setPassword("encoded");
		userEntity.setPermVersion(1L);
		when(userMapper.selectByCredential(CredentialDimension.USERNAME, "u", false)).thenReturn(userEntity);

		UsernamePasswordLoginStrategy strategy = createStrategy();

		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("u");
		command.setPassword("p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USER_DISABLED, exception.getResultCode());
		verify(loginAuditService).auditAccountDisabled(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(1L), eq("u"), eq(AuthResultCode.USER_DISABLED.getMessageKey()));
		verifyNoInteractions(passwordEncoder);
	}

	@Test
	@DisplayName("supports 返回 UsernamePasswordCommand.class")
	void supports_shouldReturnUsernamePasswordCommandClass() {
		UsernamePasswordLoginStrategy strategy = createStrategy();

		assertEquals(UsernamePasswordCommand.class, strategy.supports());
	}

	@Test
	@DisplayName("密码错误时抛出 USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING")
	void authenticate_shouldThrowRemaining_whenPasswordMismatch() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUsername("u");
		userEntity.setStatus(1);
		userEntity.setPassword("encoded");
		userEntity.setPermVersion(1L);
		when(userMapper.selectByCredential(CredentialDimension.USERNAME, "u", false)).thenReturn(userEntity);

		when(passwordEncoder.matches("p", "encoded")).thenReturn(false);
		doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);
		when(loginFailureRateLimiter.recordFailure(1L)).thenReturn(2);

		UsernamePasswordLoginStrategy strategy = createStrategy();

		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername("u");
		command.setPassword("p");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, exception.getResultCode());
		verify(loginAuditService).auditBadCredentialsWithRemaining(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_PASSWORD), eq(1L), eq("u"), eq(2));
	}

}
