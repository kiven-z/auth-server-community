package com.auth.service.auth.support.login;

import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.value.login.CompletedLoginSession;
import com.auth.service.auth.model.value.login.LoginResult;
import com.auth.service.auth.model.value.login.LogoutSessionHint;
import com.auth.service.auth.model.value.login.command.BaseLoginCommand;
import com.auth.service.auth.support.login.strategy.LoginStrategy;
import com.auth.service.auth.support.session.UserSessionRedisStore;
import com.auth.service.auth.support.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录服务：策略路由、凭证校验，会话签发委托 {@link LoginSessionOrchestrator}
 *
 * @author Bunny
 */
@Service
public class LoginService {

	private final Map<Class<? extends BaseLoginCommand>, LoginStrategy<?>> strategyRegistry;

	private final LoginSessionOrchestrator loginSessionOrchestrator;

	private final UserSessionRedisStore userSessionRedisStore;

	private final TokenService tokenService;

	private final LoginAuditService loginAuditService;

	public LoginService(List<LoginStrategy<?>> strategies, LoginSessionOrchestrator loginSessionOrchestrator,
			UserSessionRedisStore userSessionRedisStore, TokenService tokenService,
			LoginAuditService loginAuditService) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.loginSessionOrchestrator = loginSessionOrchestrator;
		this.userSessionRedisStore = userSessionRedisStore;
		this.tokenService = tokenService;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 执行登录并返回已填充访问/刷新令牌的会话读模型
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录会话读模型
	 */
	@SuppressWarnings("unchecked")
	public CompletedLoginSession login(BaseLoginCommand command, HttpServletRequest request) {
		LoginStrategy<?> strategy = lookupStrategyWithAudit(command, request);
		LoginResult loginResult = ((LoginStrategy<BaseLoginCommand>) strategy).authenticate(command, request);
		boolean rememberMe = command.getRememberMe() != null && command.getRememberMe();
		return loginSessionOrchestrator.issueSession(loginResult, rememberMe, request);
	}

	/**
	 * 查找登录策略并记录审计日志
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录策略
	 */
	private LoginStrategy<?> lookupStrategyWithAudit(BaseLoginCommand command, HttpServletRequest request) {
		try {
			LoginStrategy<?> raw = strategyRegistry.get(command.getClass());
			if (raw == null) {
				raw = strategyRegistry.entrySet()
					.stream()
					.filter(entry -> entry.getKey().isInstance(command))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElseThrow(() -> new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED));
			}
			return raw;
		}
		catch (AuthBusinessException ex) {
			loginAuditService.auditUnsupportedLoginCommand(request, command, ex.getMessageKey());
			throw ex;
		}
	}

	/**
	 * 登出当前会话（永不抛出；会话或索引不存在视为成功）
	 * @param accessToken Access Token（不含 Bearer 前缀）
	 */
	public Optional<LogoutSessionHint> logout(String accessToken) {
		return tokenService.parseAccessTokenSafe(accessToken).map(userToken -> {
			String jti = userToken.getJti();
			Long userId = userToken.getUserId();

			userSessionRedisStore.terminateSession(userId, jti);
			return LogoutSessionHint.builder().userId(userId).jti(jti).build();
		});
	}

}
