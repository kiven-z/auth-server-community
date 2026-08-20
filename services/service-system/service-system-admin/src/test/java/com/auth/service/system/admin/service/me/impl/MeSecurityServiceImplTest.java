package com.auth.service.system.admin.service.me.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.service.admin.LogLoginService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.query.UserSessionQueryOperations;
import com.auth.service.system.authorization.dispatch.query.UserSessionSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MeSecurityServiceImpl} 单元测试
 */
@DisplayName("MeSecurityServiceImpl 个人中心安全活动")
@ExtendWith(MockitoExtension.class)
class MeSecurityServiceImplTest {

	@Mock
	private UserSessionQueryOperations userSessionQueryOperations;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private LogLoginService logLoginService;

	@InjectMocks
	private MeSecurityServiceImpl meSecurityService;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	@DisplayName("查询会话：标记当前设备")
	void listMySessionsMarksCurrentSession() {
		setCurrentUser();

		UserSessionSnapshot current = UserSessionSnapshot.builder().sessionId("current-jti").build();
		UserSessionSnapshot other = UserSessionSnapshot.builder().sessionId("other-jti").build();
		when(userSessionQueryOperations.listActiveSessions(100L)).thenReturn(List.of(current, other));

		var sessions = meSecurityService.listMySessions();

		assertThat(sessions).hasSize(2);
		assertThat(sessions.get(0).getCurrent()).isTrue();
		assertThat(sessions.get(1).getCurrent()).isFalse();
	}

	@Test
	@DisplayName("踢出会话：委托 UserSessionQueryOperations")
	void kickSessionDelegatesToQueryOperations() {
		setCurrentUser();

		meSecurityService.kickSession("jti-1");

		verify(userSessionQueryOperations).kickSession(100L, "jti-1");
	}

	@Test
	@DisplayName("分页查询登录日志：强制当前用户与180天窗口并返回精简字段")
	void getLoginLogPageForcesUserAndWindowAndMapsSlimVo() {
		AuthProfile profile = AuthProfile.builder().userId(100L).username("tester").build();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(profile, null));

		MeLoginLogPageQuery query = new MeLoginLogPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		query.setLoginType("PASSWORD");
		query.setLoginResult(1);

		LogLoginLogPageVO adminVo = new LogLoginLogPageVO();
		adminVo.setLoginTime(LocalDateTime.of(2026, 1, 1, 10, 0).toInstant(java.time.ZoneOffset.UTC));
		adminVo.setLoginRegion("Shanghai");
		adminVo.setLoginResult(1);
		adminVo.setLoginType("PASSWORD");
		adminVo.setUserId(100L);
		adminVo.setUsername("tester");
		PageResponse<LogLoginLogPageVO> adminPage = PageResponse.of(1L, 10L, 1L, List.of(adminVo));
		when(logLoginService.getPage(any(LogLoginLogQuery.class))).thenReturn(adminPage);

		PageResponse<MeLoginLogPageVO> result = meSecurityService.getLoginLogPage(query);

		ArgumentCaptor<LogLoginLogQuery> queryCaptor = ArgumentCaptor.forClass(LogLoginLogQuery.class);
		verify(logLoginService).getPage(queryCaptor.capture());
		LogLoginLogQuery internalQuery = queryCaptor.getValue();
		assertThat(internalQuery.getUserId()).isEqualTo(100L);
		assertThat(internalQuery.getLoginType()).isEqualTo("PASSWORD");
		assertThat(internalQuery.getLoginResult()).isEqualTo(1);
		assertThat(internalQuery.getLoginTimeStart()).isNotNull();
		assertThat(internalQuery.getLoginTimeEnd()).isNotNull();
		assertThat(internalQuery.getLoginTimeStart()).isBeforeOrEqualTo(internalQuery.getLoginTimeEnd());

		assertThat(result.getList()).hasSize(1);
		MeLoginLogPageVO meVo = result.getList().get(0);
		assertThat(meVo.getLoginType()).isEqualTo("PASSWORD");
		assertThat(meVo.getLoginResult()).isEqualTo(1);
		assertThat(meVo.getLoginRegion()).isEqualTo("Shanghai");
	}

	private void setCurrentUser() {
		AuthProfile profile = AuthProfile.builder().userId(100L).username("tester").build();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(profile, null));
		when(userReferenceChecker.getExistingActive(100L)).thenReturn(new UserEntity());

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute(SecurityRequestAttributes.SESSION_ID, "current-jti");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

}
