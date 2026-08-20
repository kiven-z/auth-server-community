package com.auth.service.auth.support.login.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.model.value.login.LoginAccount;
import com.auth.service.auth.model.value.login.command.EmailCommand;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.redis.store.LoginVerificationCodeStore;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 邮箱验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class EmailLoginStrategy extends AbstractLoginStrategyTemplate<EmailCommand> {

	private final LoginVerificationCodeStore loginVerificationCodeStore;

	private final UserMapper userMapper;

	public EmailLoginStrategy(AuthProfileRepository authProfileRepository, LoginAuditService loginAuditService,
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
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LoginAccount resolveAccount(EmailCommand command) {
		String email = command.getEmail();
		UserEntity userEntity = userMapper.selectByCredential(CredentialDimension.EMAIL, email, false);

		if (userEntity == null) {
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}

		return LoginAccount.from(userEntity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AuthLoginLogType doAuthenticate(EmailCommand command, LoginAccount account) {
		String target = Objects.requireNonNull(command.getEmail());
		String key = CredentialDimension.resolveKey(CredentialDimension.EMAIL, target);

		loginVerificationCodeStore.verifyAndConsume(key, command.getCode());
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<EmailCommand> supports() {
		return EmailCommand.class;
	}

}
