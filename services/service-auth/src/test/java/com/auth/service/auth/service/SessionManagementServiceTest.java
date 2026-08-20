package com.auth.service.auth.service;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.query.OnlineUserPageQuery;
import com.auth.service.auth.model.value.session.OnlineUserPageSlice;
import com.auth.service.auth.model.value.session.OnlineUserPageSlice.OnlineUserEntry;
import com.auth.service.auth.model.vo.UserSessionVO;
import com.auth.service.auth.service.impl.SessionManagementServiceImpl;
import com.auth.service.auth.support.session.OnlineUserSessionReader;
import com.auth.service.auth.support.session.UserSessionRedisStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SessionManagementService} 单元测试
 */
@DisplayName("SessionManagementService 会话管理")
@ExtendWith(MockitoExtension.class)
class SessionManagementServiceTest {

	@Mock
	private UserSessionRedisStore userSessionRedisStore;

	@Mock
	private OnlineUserSessionReader onlineUserSessionReader;

	@Mock
	private UserMapper userMapper;

	private SessionManagementService sessionManagementService;

	@BeforeEach
	void setUp() {
		sessionManagementService = new SessionManagementServiceImpl(userSessionRedisStore, onlineUserSessionReader,
				userMapper);
	}

	@Test
	@DisplayName("在线用户分页：无用户筛选时 allowedUserIds 为 null")
	void getOnlineUserPage_shouldQueryAllWhenUserIdBlank() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);

		OnlineUserEntry entry = OnlineUserEntry.builder()
			.userId(TestConstants.USER_ID)
			.lastLoginAt(1_700_000_000_000L)
			.activeSessionCount(2)
			.build();
		when(onlineUserSessionReader.pageOnlineUsers(1, 20, null))
			.thenReturn(OnlineUserPageSlice.builder().total(1L).users(List.of(entry)).build());

		UserEntity userEntity = new UserEntity();
		userEntity.setId(TestConstants.USER_ID);
		userEntity.setUsername("alice");
		userEntity.setNickname("Alice");
		when(userMapper.selectList(any())).thenReturn(List.of(userEntity));

		var page = sessionManagementService.getOnlineUserPage(query);

		verify(onlineUserSessionReader).pageOnlineUsers(1, 20, null);
		assertEquals(1L, page.getTotal());
		assertEquals(1, page.getList().size());
		assertEquals("alice", page.getList().get(0).getUsername());
		assertEquals(2, page.getList().get(0).getActiveSessionCount());
	}

	@Test
	@DisplayName("在线用户分页：传入 userId 时按单用户筛选")
	void getOnlineUserPage_shouldPassAllowedUserIdsWhenUserIdProvided() {
		OnlineUserPageQuery query = new OnlineUserPageQuery();
		query.setUserId(TestConstants.USER_ID);
		when(onlineUserSessionReader.pageOnlineUsers(1, 30, Set.of(TestConstants.USER_ID)))
			.thenReturn(OnlineUserPageSlice.builder().total(0L).users(List.of()).build());

		sessionManagementService.getOnlineUserPage(query);

		verify(onlineUserSessionReader).pageOnlineUsers(1, 30, Set.of(TestConstants.USER_ID));
		verifyNoInteractions(userMapper);
	}

	@Test
	@DisplayName("踢出指定会话：委托 UserSessionRedisStore.terminateSession")
	void kickSessionShouldDelegateToStore() {
		sessionManagementService.kickSession(TestConstants.USER_ID, TestConstants.JTI);

		verify(userSessionRedisStore).terminateSession(TestConstants.USER_ID, TestConstants.JTI);
	}

	@Test
	@DisplayName("踢出用户全部会话：委托 UserSessionRedisStore.terminateAllSessions")
	void kickAllSessionsByUserIdShouldDelegateToStore() {
		sessionManagementService.kickAllSessions(TestConstants.USER_ID);

		verify(userSessionRedisStore).terminateAllSessions(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("批量踢出全部会话：委托 UserSessionRedisStore.terminateAllSessions")
	void kickAllSessionsByUserIdsShouldDelegateToStore() {
		List<Long> userIds = List.of(TestConstants.USER_ID, 2L);

		sessionManagementService.kickAllSessions(userIds);

		verify(userSessionRedisStore).terminateAllSessions(TestConstants.USER_ID);
		verify(userSessionRedisStore).terminateAllSessions(2L);
	}

	@Test
	@DisplayName("批量踢会话：入参为空/含 null 时不抛异常且跳过 null；下游异常会被吞掉")
	void kickAllSessionsBatch_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		assertDoesNotThrow(() -> sessionManagementService.kickAllSessions(List.of()));
		verifyNoInteractions(userSessionRedisStore);

		doThrow(new RuntimeException("boom")).when(userSessionRedisStore).terminateAllSessions(TestConstants.USER_ID);
		assertDoesNotThrow(() -> sessionManagementService.kickAllSessions(Arrays.asList(null, TestConstants.USER_ID)));
		verify(userSessionRedisStore).terminateAllSessions(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("查询活跃会话：委托 UserSessionRedisStore 并映射为 VO")
	void listActiveSessionsShouldDelegateToStore() {
		UserSessionIndex index = new UserSessionIndex();
		index.setUserId(TestConstants.USER_ID);
		index.setSessionId(TestConstants.JTI);
		when(userSessionRedisStore.listUserSessions(TestConstants.USER_ID)).thenReturn(List.of(index));

		List<UserSessionVO> result = sessionManagementService.listActiveSessions(TestConstants.USER_ID);

		verify(userSessionRedisStore).listUserSessions(TestConstants.USER_ID);
		assertEquals(1, result.size());
		assertEquals(TestConstants.JTI, result.get(0).getSessionId());
	}

}
