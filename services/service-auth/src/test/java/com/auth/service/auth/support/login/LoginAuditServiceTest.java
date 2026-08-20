package com.auth.service.auth.support.login;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.value.login.LoginAuditSnapshot;
import com.auth.service.auth.support.redis.AuthProfileRedisCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link LoginAuditService} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginAuditServiceTest {

	@Mock
	private LoginLogRepository loginLogRepository;

	@Mock
	private AuthProfileRedisCache authProfileRedisCache;

	@Mock
	private UserMapper userMapper;

	private LoginAuditService loginAuditService;

	@BeforeEach
	void setUp() {
		loginAuditService = new LoginAuditService(loginLogRepository, authProfileRedisCache, userMapper);
	}

	@Test
	@DisplayName("刷新成功审计：请求线程构建快照并规范化主体后异步落库")
	void auditRefreshTokenSuccess_shouldBuildSnapshotAndRecordAsync() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginLogRepository.buildSnapshot(null)).thenReturn(snapshot);
		when(authProfileRedisCache.loadCachedProfile(TestConstants.USER_ID)).thenReturn(
				Optional.of(AuthProfile.builder().userId(TestConstants.USER_ID).username("Administrator").build()));

		loginAuditService.auditRefreshTokenSuccess(null, TestConstants.USER_ID, TestConstants.JTI);

		verify(loginLogRepository).buildSnapshot(null);
		verify(loginLogRepository).recordLoginLog(same(snapshot), eq(AuthLoginLogType.REFRESH_TOKEN),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
	}

	@Test
	@DisplayName("无 userId：规范化时保留登录尝试主体")
	void normalizePrincipal_shouldKeepAttemptPrincipal_whenUserIdNull() {
		when(loginLogRepository.buildSnapshot(null)).thenReturn(LoginAuditSnapshot.empty());

		loginAuditService.auditUserUnknownAfterResolve(null, AuthLoginLogType.LOGIN_PASSWORD, "13800138000",
				"user.not.found");

		verify(loginLogRepository).recordLoginLog(any(), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("13800138000"), eq("user.not.found"),
				isNull());
		verifyNoInteractions(authProfileRedisCache, userMapper);
	}

	@Test
	@DisplayName("有 userId 且入参合法：规范化保留入参登录名")
	void normalizePrincipal_shouldKeepIncoming_whenUsablePrincipal() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginLogRepository.buildSnapshot(null)).thenReturn(snapshot);

		loginAuditService.auditLoginSuccess(null, AuthLoginLogType.LOGIN_PASSWORD, TestConstants.USER_ID,
				"Administrator", TestConstants.JTI);

		verify(loginLogRepository).recordLoginLog(same(snapshot), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
		verifyNoInteractions(authProfileRedisCache, userMapper);
	}

	@Test
	@DisplayName("有 userId 且入参为历史占位符：从画像缓存解析登录名")
	void normalizePrincipal_shouldResolveFromCache_whenPlaceholderPrincipal() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginLogRepository.buildSnapshot(null)).thenReturn(snapshot);
		when(authProfileRedisCache.loadCachedProfile(TestConstants.USER_ID)).thenReturn(
				Optional.of(AuthProfile.builder().userId(TestConstants.USER_ID).username("Administrator").build()));

		loginAuditService.auditLoginSuccess(null, AuthLoginLogType.LOGIN_PASSWORD, TestConstants.USER_ID, "/",
				TestConstants.JTI);

		verify(loginLogRepository).recordLoginLog(same(snapshot), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
		verifyNoInteractions(userMapper);
	}

	@Test
	@DisplayName("有 userId 且缓存未命中：从 UserMapper 解析登录名")
	void normalizePrincipal_shouldResolveFromDb_whenCacheMiss() {
		LoginAuditSnapshot snapshot = LoginAuditSnapshot.empty();
		when(loginLogRepository.buildSnapshot(null)).thenReturn(snapshot);
		when(authProfileRedisCache.loadCachedProfile(TestConstants.USER_ID)).thenReturn(Optional.empty());
		UserEntity user = new UserEntity();
		user.setId(TestConstants.USER_ID);
		user.setUsername("Administrator");
		when(userMapper.selectById(TestConstants.USER_ID)).thenReturn(user);

		loginAuditService.auditLoginSuccess(null, AuthLoginLogType.LOGIN_PASSWORD, TestConstants.USER_ID, null,
				TestConstants.JTI);

		verify(loginLogRepository).recordLoginLog(same(snapshot), eq(AuthLoginLogType.LOGIN_PASSWORD),
				eq(AuthLoginLogResult.SUCCESS), eq(TestConstants.USER_ID), eq("Administrator"), isNull(),
				eq(TestConstants.JTI));
	}

	@Test
	@DisplayName("resolvePrincipal：空白主体返回 null")
	void resolvePrincipal_shouldReturnNull_whenBlank() {
		assertNull(loginAuditService.resolvePrincipal(null));
	}

}
