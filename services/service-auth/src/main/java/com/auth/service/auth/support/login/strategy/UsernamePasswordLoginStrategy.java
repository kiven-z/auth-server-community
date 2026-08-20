package com.auth.service.auth.support.login.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.exception.AuthBadCredentialsException;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.model.value.login.LoginAccount;
import com.auth.service.auth.model.value.login.command.UsernamePasswordCommand;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.login.LoginAuditService;
import com.auth.service.auth.support.token.LoginFailureRateLimiter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户名密码认证策略：查询用户并用 {@link PasswordEncoder} 校验密码。
 *
 * @author Bunny
 */
@Component
public class UsernamePasswordLoginStrategy extends AbstractLoginStrategyTemplate<UsernamePasswordCommand> {

	private final PasswordEncoder passwordEncoder;

	private final UserMapper userMapper;

	public UsernamePasswordLoginStrategy(AuthProfileRepository authProfileRepository, PasswordEncoder passwordEncoder,
			LoginAuditService loginAuditService, LoginFailureRateLimiter loginFailureRateLimiter,
			UserMapper userMapper) {
		super(authProfileRepository, loginAuditService, loginFailureRateLimiter);
		this.passwordEncoder = passwordEncoder;
		this.userMapper = userMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LoginAccount resolveAccount(UsernamePasswordCommand command) {
		String username = command.getUsername();
		UserEntity userEntity = userMapper.selectByCredential(CredentialDimension.USERNAME, username, false);
		if (userEntity == null) {
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}

		return LoginAccount.from(userEntity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AuthLoginLogType doAuthenticate(UsernamePasswordCommand command, LoginAccount account) {
		if (!passwordEncoder.matches(command.getPassword(), account.encodedPassword())) {
			throw new AuthBadCredentialsException("Password mismatch");
		}
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<UsernamePasswordCommand> supports() {
		return UsernamePasswordCommand.class;
	}

}
