package com.auth.service.system.admin.service.me.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeUserSessionVO;
import com.auth.service.system.admin.service.admin.LogLoginService;
import com.auth.service.system.admin.service.me.MeSecurityService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.query.UserSessionQueryOperations;
import com.auth.service.system.authorization.dispatch.query.UserSessionSnapshot;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 个人中心安全活动服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MeSecurityServiceImpl implements MeSecurityService {

	/**
	 * 个人中心登录日志最大可查天数
	 */
	private static final int ME_LOGIN_LOG_MAX_DAYS = 180;

	private final UserSessionQueryOperations userSessionQueryOperations;

	private final UserReferenceChecker userReferenceChecker;

	private final LogLoginService logLoginService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MeUserSessionVO> listMySessions() {
		Long userId = SecurityUserUtils.getUserId();
		userReferenceChecker.getExistingActive(userId);

		String currentSessionId = SecurityUserUtils.getSessionId();
		List<UserSessionSnapshot> sessions = userSessionQueryOperations.listActiveSessions(userId);

		return sessions.stream().map(session -> {
			MeUserSessionVO vo = new MeUserSessionVO();
			vo.setSessionId(session.getSessionId());
			vo.setIpAddress(session.getIpAddress());
			vo.setIpRegion(session.getIpRegion());
			vo.setDeviceType(session.getDeviceType());
			vo.setBrowserType(session.getBrowserType());
			vo.setOsType(session.getOsType());
			vo.setRememberMe(session.getRememberMe());
			vo.setRefreshTokenExpiresAt(session.getRefreshTokenExpiresAt());
			vo.setLoginAt(session.getLoginAt());
			vo.setCurrent(Objects.equals(session.getSessionId(), currentSessionId));
			return vo;
		}).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void kickSession(String sessionId) {
		Long userId = SecurityUserUtils.getUserId();
		userReferenceChecker.getExistingActive(userId);

		userSessionQueryOperations.kickSession(userId, sessionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<MeLoginLogPageVO> getLoginLogPage(MeLoginLogPageQuery query) {
		Long userId = SecurityUserUtils.getUserId();
		if (userId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.JWT_INVALID);
		}
		LogLoginLogQuery internalQuery = new LogLoginLogQuery();
		internalQuery.setPageIndex(query.getPageIndex());
		internalQuery.setPageSize(query.getPageSize());
		internalQuery.setLoginType(query.getLoginType());
		internalQuery.setLoginResult(query.getLoginResult());
		internalQuery.setUserId(userId);

		Instant now = Instant.now();
		internalQuery.setLoginTimeStart(now.minus(ME_LOGIN_LOG_MAX_DAYS, ChronoUnit.DAYS));
		internalQuery.setLoginTimeEnd(now);
		PageResponse<LogLoginLogPageVO> adminPage = logLoginService.getPage(internalQuery);

		List<MeLoginLogPageVO> meList = adminPage.getList().stream().map(adminVo -> {
			MeLoginLogPageVO vo = new MeLoginLogPageVO();
			vo.setLoginTime(adminVo.getLoginTime());
			vo.setLoginRegion(adminVo.getLoginRegion());
			vo.setLoginResult(adminVo.getLoginResult());
			vo.setLoginType(adminVo.getLoginType());
			return vo;
		}).toList();
		return PageResponse.of(adminPage.getPageNo(), adminPage.getPageSize(), adminPage.getTotal(), meList);
	}

}
