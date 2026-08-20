package com.auth.service.auth.support.session;

import com.auth.module.security.autoconfigure.config.user.SessionLimitStrategy;
import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * 登录前会话数量上限校验与淘汰策略。
 *
 * @author Bunny
 */
@Component
public class SessionLimitGuard {

	private final UserConfigProperties userConfigProperties;

	private final UserSessionRedisStore userSessionRedisStore;

	public SessionLimitGuard(UserConfigProperties userConfigProperties, UserSessionRedisStore userSessionRedisStore) {
		this.userConfigProperties = userConfigProperties;
		this.userSessionRedisStore = userSessionRedisStore;
	}

	/**
	 * 校验用户活跃会话是否超过配置上限；必要时按策略淘汰最旧会话。
	 * @param userId 用户 ID
	 */
	public void assertSessionLimit(Long userId) {
		Assert.notNull(userId, "userId must not be null");
		userSessionRedisStore.cleanupStaleActiveSessions(userId);
		userSessionRedisStore.syncOnlineUserAfterTerminate(userId);
		Integer maxSessionCount = userConfigProperties.getMaxSessionCount();
		if (maxSessionCount == null || maxSessionCount <= 0) {
			return;
		}

		long sessionCount = userSessionRedisStore.countActiveSessions(userId);
		if (sessionCount < maxSessionCount) {
			return;
		}

		SessionLimitStrategy limitStrategy = userConfigProperties.getSessionLimitStrategy();
		if (limitStrategy == null) {
			throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
		}
		switch (limitStrategy) {
			case EVICT_OLDEST -> {
				Optional<String> evicted = userSessionRedisStore.evictOldestActiveSession(userId);
				evicted.ifPresent(jti -> userSessionRedisStore.syncOnlineUserAfterTerminate(userId));

				long sessionCountAfterEvict = userSessionRedisStore.countActiveSessions(userId);
				if (sessionCountAfterEvict >= maxSessionCount) {
					throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
				}
			}
			// 达到上限时拒绝本次登录
			case REJECT_LOGIN -> throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
			default -> throw new AuthBusinessException(AuthResultCode.MAX_SESSION_LIMIT_EXCEEDED);
		}
	}

}
