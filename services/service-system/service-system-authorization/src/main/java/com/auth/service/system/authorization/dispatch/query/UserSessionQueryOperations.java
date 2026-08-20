package com.auth.service.system.authorization.dispatch.query;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.authorization.feign.MeSessionInternalFeignClient;
import com.auth.service.system.authorization.feign.dto.UserSessionRemoteDTO;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_FAILED;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;

/**
 * 经 auth 内部接口查询与踢出用户会话。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserSessionQueryOperations {

	private final MeSessionInternalFeignClient meSessionInternalFeignClient;

	/**
	 * 查询用户活跃会话列表。
	 * @param userId 用户 ID
	 * @return 活跃会话快照列表
	 */
	public List<UserSessionSnapshot> listActiveSessions(Long userId) {
		Result<List<UserSessionRemoteDTO>> result = meSessionInternalFeignClient.listUserSessions(userId);
		if (!FeignUtil.isSuccess(result)) {
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}

		return CollUtil.emptyIfNull(result.getData())
			.stream()
			.map(session -> UserSessionSnapshot.builder()
				.sessionId(session.getSessionId())
				.ipAddress(session.getIpAddress())
				.ipRegion(session.getIpRegion())
				.deviceType(session.getDeviceType())
				.browserType(session.getBrowserType())
				.osType(session.getOsType())
				.rememberMe(session.getRememberMe())
				.refreshTokenExpiresAt(session.getRefreshTokenExpiresAt())
				.loginAt(session.getLoginAt())
				.build())
			.toList();
	}

	/**
	 * 踢出用户指定会话。
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 */
	public void kickSession(Long userId, String sessionId) {
		Result<Void> result = meSessionInternalFeignClient.kickUserSession(userId, sessionId);
		if (!FeignUtil.isSuccess(result)) {
			throw new SystemBusinessException(OPERATION_FAILED);
		}
	}

}
