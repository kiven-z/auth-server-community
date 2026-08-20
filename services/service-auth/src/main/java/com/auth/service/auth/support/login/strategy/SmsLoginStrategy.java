package com.auth.service.auth.support.login.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.model.value.login.LoginAccount;
import com.auth.service.auth.model.value.login.command.SmsCommand;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.redis.store.LoginVerificationCodeStore;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 短信验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class SmsLoginStrategy extends AbstractLoginStrategyTemplate<SmsCommand> {

	private final LoginVerificationCodeStore loginVerificationCodeStore;

	private final UserMapper userMapper;

	public SmsLoginStrategy(AuthProfileRepository authProfileRepository, LoginAuditService loginAuditService,
			LoginFailureRateLimiter loginFailureRateLimiter, LoginVerificationCodeStore loginVerificationCodeStore,
			UserMapper userMapper) {
		super(authProfileRepository, loginAuditService, loginFailureRateLimiter);
		this.loginVerificationCodeStore = loginVerificationCodeStore;
		this.userMapper = userMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LoginAccount resolveAccount(SmsCommand command) {
		String phone = command.getPhone();
		UserEntity userEntity = userMapper.selectByCredential(CredentialDimension.PHONE, phone, false);

		if (userEntity == null) {
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}

		return LoginAccount.from(userEntity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AuthLoginLogType doAuthenticate(SmsCommand command, LoginAccount account) {
		String target = Objects.requireNonNull(command.getPhone());
		String key = CredentialDimension.resolveKey(CredentialDimension.PHONE, target);

		loginVerificationCodeStore.verifyAndConsume(key, command.getCode());
		return AuthLoginLogType.LOGIN_SMS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<SmsCommand> supports() {
		return SmsCommand.class;
	}

}
