package com.auth.service.auth.support.login.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.model.value.login.command.SmsCommand;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.redis.store.LoginVerificationCodeStore;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * {@link SmsLoginStrategy} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class SmsLoginStrategyTest {

	@Mock
	private AuthProfileRepository authProfileRepository;

	@Mock
	private LoginAuditService loginAuditService;

	@Mock
	private LoginVerificationCodeStore loginVerificationCodeStore;

	@Mock
	private LoginFailureRateLimiter loginFailureRateLimiter;

	@Mock
	private UserMapper userMapper;

	@Mock
	private HttpServletRequest httpServletRequest;

	private SmsLoginStrategy createStrategy() {
		return new SmsLoginStrategy(authProfileRepository, loginAuditService, loginFailureRateLimiter,
				loginVerificationCodeStore, userMapper);
	}

	@Test
	@DisplayName("手机号未注册时抛出 USERNAME_OR_PASSWORD_ERROR 并发布审计事件")
	void authenticate_shouldThrow_whenPhoneNotRegistered() {
		when(userMapper.selectByCredential(CredentialDimension.PHONE, "13800138000", false)).thenReturn(null);
		SmsLoginStrategy strategy = createStrategy();

		SmsCommand command = new SmsCommand();
		command.setPhone("13800138000");
		command.setCode("123456");
		when(loginAuditService.resolvePrincipal(command)).thenReturn("13800138000");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditService).resolvePrincipal(command);
		verify(loginAuditService).auditUserUnknownAfterResolve(httpServletRequest, AuthLoginLogType.LOGIN_SMS,
				"13800138000", exception.getMessageKey());
	}

	@Test
	@DisplayName("验证码错误时抛出 AUTH_CODE_ERROR")
	void doAuthenticate_shouldThrow_whenCodeMismatch() {
		String phone = "13800138000";
		String code = "000000";
		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUsername("testUser");
		userEntity.setStatus(1);
		userEntity.setPermVersion(1L);
		when(userMapper.selectByCredential(CredentialDimension.PHONE, phone, false)).thenReturn(userEntity);
		doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);
		doThrow(new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR)).when(loginVerificationCodeStore)
			.verifyAndConsume(CredentialDimension.resolveKey(CredentialDimension.PHONE, phone), code);

		SmsLoginStrategy strategy = createStrategy();

		SmsCommand command = new SmsCommand();
		command.setPhone(phone);
		command.setCode(code);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.AUTH_CODE_ERROR, exception.getResultCode());
		verify(loginAuditService).auditMappedCredentialFailure(httpServletRequest, AuthLoginLogType.LOGIN_SMS, 1L,
				"testUser", exception);
	}

	@Test
	@DisplayName("验证码正确时登录成功并删除 Redis 中的验证码")
	void doAuthenticate_shouldSucceed_whenCodeMatches() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUsername("testUser");
		userEntity.setStatus(1);
		userEntity.setNickname("Test");
		userEntity.setPermVersion(1L);
		when(userMapper.selectByCredential(CredentialDimension.PHONE, "13800138000", false)).thenReturn(userEntity);
		doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);

		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileRepository.buildByUserId(1L)).thenReturn(profile);

		String phone = "13800138000";
		String code = "123456";

		SmsLoginStrategy strategy = createStrategy();
		SmsCommand command = new SmsCommand();
		command.setPhone(phone);
		command.setCode(code);

		strategy.authenticate(command, httpServletRequest);

		verify(loginVerificationCodeStore)
			.verifyAndConsume(CredentialDimension.resolveKey(CredentialDimension.PHONE, phone), code);
	}

	@Test
	@DisplayName("supports 返回 SmsCommand.class")
	void supports_shouldReturnSmsCommandClass() {
		SmsLoginStrategy strategy = createStrategy();
		assertEquals(SmsCommand.class, strategy.supports());
	}

}
