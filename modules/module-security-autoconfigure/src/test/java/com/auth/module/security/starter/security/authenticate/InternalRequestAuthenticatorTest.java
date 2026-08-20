package com.auth.module.security.starter.security.authenticate;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.autoconfigure.pipeline.authenticate.InternalRequestAuthenticator;
import com.auth.module.security.autoconfigure.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalRequestAuthenticatorTest {

	@Mock
	private InternalTokenProvider internalTokenProvider;

	@Mock
	private AuthProfileCacheService authProfileCacheService;

	@Test
	@DisplayName("测试支持判断：存在 X-Internal-JWT 时返回 true")
	void supports_shouldReturnTrue_whenInternalHeaderPresent() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider,
				authProfileCacheService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "internal-token");

		assertTrue(authenticator.supports(request));
	}

	@Test
	@DisplayName("测试内部服务令牌认证成功并写入服务角色")
	void authenticate_shouldReturnServiceProfile_whenPrincipalTypeIsService() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider,
				authProfileCacheService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "internal-token");
		when(internalTokenProvider.parseToken("internal-token")).thenReturn(SecurityTokenResult.builder()
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE)
			.serviceId("service-auth")
			.build());

		var profile = authenticator.authenticate(request);

		assertEquals("service-auth", profile.getUsername());
		assertNotNull(profile.getRoles());
		assertTrue(profile.getRoles().contains(SecurityInternalTokenConstants.ROLE_INTERNAL_SERVICE));
		verify(internalTokenProvider).parseToken("internal-token");
	}

	@Test
	@DisplayName("测试用户身份令牌认证成功并从缓存加载用户画像")
	void authenticate_shouldReturnUserProfile_whenPrincipalTypeIsUser() {
		// 模拟 Redis 缓存中的用户画像
		AuthProfile cachedProfile = AuthProfile.builder()
			.userId(1001L)
			.username("testuser")
			.roles(List.of("ROLE_USER", "ROLE_ADMIN"))
			.permissions(List.of("user:read", "user:write"))
			.permVersion(5L)
			.build();

		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider,
				authProfileCacheService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "user-token");

		JwtUserToken userToken = JwtUserToken.builder().userId(1001L).build();

		when(internalTokenProvider.parseToken("user-token")).thenReturn(SecurityTokenResult.builder()
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_USER)
			.userToken(userToken)
			.permVersion(5L)
			.build());

		when(authProfileCacheService.load(1001L)).thenReturn(cachedProfile);

		var profile = authenticator.authenticate(request);

		// 验证返回的是缓存中的用户画像
		assertEquals(1001L, profile.getUserId());
		assertEquals("testuser", profile.getUsername());
		assertEquals(2, profile.getRoles().size());
		assertTrue(profile.getRoles().contains("ROLE_USER"));
		assertTrue(profile.getRoles().contains("ROLE_ADMIN"));
		assertEquals(2, profile.getPermissions().size());

		verify(internalTokenProvider).parseToken("user-token");
		verify(authProfileCacheService).load(1001L);
	}

	@Test
	@DisplayName("测试用户身份令牌但缓存未命中时抛出异常")
	void authenticate_shouldThrowProfileCacheMiss_whenUserProfileNotInCache() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider,
				authProfileCacheService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "user-token");

		JwtUserToken userToken = JwtUserToken.builder().userId(1001L).build();

		when(internalTokenProvider.parseToken("user-token")).thenReturn(SecurityTokenResult.builder()
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_USER)
			.userToken(userToken)
			.permVersion(5L)
			.build());

		when(authProfileCacheService.load(1001L)).thenReturn(null);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> authenticator.authenticate(request));
		assertEquals(SecurityResultCodeEnum.PROFILE_CACHE_MISS, ex.getResultCode());
		assertTrue(ex.getMessage().contains("User profile not found in cache"));
	}

	@Test
	@DisplayName("测试用户身份令牌权限版本不匹配时抛出异常")
	void authenticate_shouldThrowVersionMismatch_whenPermVersionMismatch() {
		AuthProfile cachedProfile = AuthProfile.builder().userId(1001L).username("testuser").permVersion(10L).build();

		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider,
				authProfileCacheService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "user-token");

		JwtUserToken userToken = JwtUserToken.builder().userId(1001L).build();

		when(internalTokenProvider.parseToken("user-token")).thenReturn(SecurityTokenResult.builder()
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_USER)
			.userToken(userToken)
			.permVersion(5L)
			.build());

		when(authProfileCacheService.load(1001L)).thenReturn(cachedProfile);

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> authenticator.authenticate(request));
		assertEquals(SecurityResultCodeEnum.PERMISSION_VERSION_MISMATCH, ex.getResultCode());
		assertTrue(ex.getMessage().contains("Permission version mismatch"));
	}

	@Test
	@DisplayName("测试用户身份令牌但 userId 为 null 时抛出异常")
	void authenticate_shouldThrowTokenInvalid_whenUserIdIsNull() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider,
				authProfileCacheService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "user-token");

		JwtUserToken userToken = JwtUserToken.builder().userId(null).build();

		when(internalTokenProvider.parseToken("user-token")).thenReturn(SecurityTokenResult.builder()
			.principalType(SecurityInternalTokenConstants.PRINCIPAL_TYPE_USER)
			.userToken(userToken)
			.build());

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> authenticator.authenticate(request));
		assertEquals(SecurityResultCodeEnum.TOKEN_INVALID, ex.getResultCode());
		assertTrue(ex.getMessage().contains("must contain valid userId"));
	}

	@Test
	@DisplayName("测试未知的 principalType 时抛出异常")
	void authenticate_shouldThrowTokenInvalid_whenPrincipalTypeIsUnknown() {
		InternalRequestAuthenticator authenticator = new InternalRequestAuthenticator(internalTokenProvider,
				authProfileCacheService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(SecurityInternalTokenConstants.INTERNAL_HEADER, "unknown-token");

		when(internalTokenProvider.parseToken("unknown-token"))
			.thenReturn(SecurityTokenResult.builder().principalType("UNKNOWN").build());

		SecurityTokenException ex = assertThrows(SecurityTokenException.class,
				() -> authenticator.authenticate(request));
		assertEquals(SecurityResultCodeEnum.TOKEN_INVALID, ex.getResultCode());
		assertTrue(ex.getMessage().contains("Unknown principal_type"));
	}

}
