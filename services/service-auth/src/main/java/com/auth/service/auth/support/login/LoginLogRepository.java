package com.auth.service.auth.support.login;

import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.mapper.LoginLogMapper;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.value.login.LoginAuditSnapshot;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * 登录审计仓储
 *
 * @author Bunny
 */
@Slf4j
@Repository
public class LoginLogRepository extends ServiceImpl<LoginLogMapper, LoginLogEntity> {

	private final IpAddressService ipAddressService;

	public LoginLogRepository(IpAddressService ipAddressService) {
		this.ipAddressService = ipAddressService;
	}

	/**
	 * 解析请求为审计快照；request == null 时返回空快照。
	 * @param request HTTP 请求，可为 null
	 * @return 不可变快照
	 */
	public LoginAuditSnapshot buildSnapshot(HttpServletRequest request) {
		if (request == null) {
			return LoginAuditSnapshot.empty();
		}
		IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
		UserAgent userAgent = UserAgent.getUserAgent(request);
		return LoginAuditSnapshot.builder()
			.loginIp(ipInfo.getIpAddr())
			.loginRegion(ipInfo.getIpRegion())
			.userAgent(request.getHeader("User-Agent"))
			.deviceType(userAgent.getDeviceType())
			.osType(userAgent.getOs())
			.browserType(userAgent.getBrowser())
			.build();
	}

	/**
	 * 记录一条认证相关审计
	 * @param snapshot 请求快照，不可为 null；无请求信息时使用 {@link LoginAuditSnapshot#empty()}
	 * @param eventType 事件类型
	 * @param result 结果码
	 * @param userId 用户 ID，未识别可为 null
	 * @param principal 用户名或邮箱等冗余检索字段
	 * @param failureReason 失败原因（messageKey 或简短说明）
	 * @param sessionId 会话 ID（如 jti）；无可为空
	 */
	@Async
	public void recordLoginLog(LoginAuditSnapshot snapshot, AuthLoginLogType eventType, AuthLoginLogResult result,
			Long userId, String principal, String failureReason, String sessionId) {
		LoginLogEntity entity = new LoginLogEntity();
		entity.setLoginIp(snapshot.loginIp());
		entity.setLoginRegion(snapshot.loginRegion());
		entity.setUserAgent(snapshot.userAgent());
		entity.setDeviceType(snapshot.deviceType());
		entity.setOsType(snapshot.osType());
		entity.setBrowserType(snapshot.browserType());

		try {
			entity.setUserId(userId);
			entity.setUsername(principal);
			entity.setLoginType(eventType.name());
			entity.setLoginResult(result.getCode());
			entity.setFailureReason(failureReason);
			entity.setLoginTime(Instant.now());
			entity.setSessionId(sessionId);

			if (userId != null) {
				entity.setCreatedBy(userId);
				entity.setUpdatedBy(userId);
			}

			baseMapper.insert(entity);
		}
		catch (Exception ex) {
			log.error(
					"Failed to persist auth audit: eventType={}, result={}, userId={}, principal={}, sessionId={}, cause={}",
					eventType, result, userId, principal, sessionId, ex, ex);
		}
	}

}
