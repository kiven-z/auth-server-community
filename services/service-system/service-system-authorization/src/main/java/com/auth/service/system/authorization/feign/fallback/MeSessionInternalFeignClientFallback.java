package com.auth.service.system.authorization.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.feign.MeSessionInternalFeignClient;
import com.auth.service.system.authorization.feign.dto.UserSessionRemoteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户会话内部 Feign 降级
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MeSessionInternalFeignClientFallback implements MeSessionInternalFeignClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<List<UserSessionRemoteDTO>> listUserSessions(Long userId) {
		log.error("MeSessionInternalFeignClientFallback: listUserSessions failed, userId={}", userId);
		return Result.error("Session query service is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<Void> kickUserSession(Long userId, String sessionId) {
		log.error("MeSessionInternalFeignClientFallback: kickUserSession failed, userId={}, sessionId={}", userId,
				sessionId);
		return Result.error("Session kick service is unavailable");
	}

}
