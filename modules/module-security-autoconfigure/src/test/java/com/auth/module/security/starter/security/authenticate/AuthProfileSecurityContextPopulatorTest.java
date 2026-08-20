package com.auth.module.security.starter.security.authenticate;

import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthProfileSecurityContextPopulatorTest {

	private AuthProfileSecurityContextPopulator populator;

	@BeforeEach
	void setUp() {
		populator = new AuthProfileSecurityContextPopulator();
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("测试设置认证角色和权限")
	void populate_setsAuthenticationWithRolesAndPermissions() {
		AuthProfile profile = AuthProfile.builder()
			.userId(100L)
			.username("alice")
			.roles(List.of("ADMIN", "ROLE_AUDITOR"))
			.permissions(List.of("user:read", "user:write"))
			.build();

		populator.populate(profile);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(auth);
		assertTrue(auth.isAuthenticated());
		assertEquals(profile, auth.getPrincipal());

		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		assertNotNull(authorities);

		List<String> authorityStrings = authorities.stream().map(GrantedAuthority::getAuthority).toList();

		// Permissions should be kept as-is
		assertTrue(authorityStrings.contains("user:read"));
		assertTrue(authorityStrings.contains("user:write"));

		// Roles without ROLE_ prefix should get ROLE_ prepended
		assertTrue(authorityStrings.contains("ROLE_ADMIN"));

		// Roles with ROLE_ prefix should NOT be double-prefixed
		assertTrue(authorityStrings.contains("ROLE_AUDITOR"));
	}

	@Test
	@DisplayName("测试没有角色和权限时应创建空权限")
	void populate_withoutRolesOrPermissions_createsEmptyAuthorities() {
		AuthProfile profile = AuthProfile.builder().userId(100L).username("alice").build();

		populator.populate(profile);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(auth);
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		assertTrue(authorities == null || authorities.isEmpty());
	}

	@Test
	@DisplayName("测试清除之前上下文")
	void populate_clearsPreviousContext() {
		AuthProfile first = AuthProfile.builder().userId(1L).username("first").build();
		AuthProfile second = AuthProfile.builder().userId(2L).username("second").build();

		populator.populate(first);
		populator.populate(second);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(auth);
		AuthProfile principal = (AuthProfile) auth.getPrincipal();
		assertEquals(2L, principal.getUserId());
	}

	@Test
	@DisplayName("runWithProfile：执行期间使用目标画像，结束后恢复原上下文")
	void runWithProfile_restoresOriginalContext() {
		AuthProfile original = AuthProfile.builder().userId(1L).username("original").build();
		AuthProfile scoped = AuthProfile.builder().userId(2L).username("scoped").build();
		populator.populate(original);

		Long result = populator.runWithProfile(scoped, () -> {
			Authentication during = SecurityContextHolder.getContext().getAuthentication();
			assertNotNull(during);
			assertEquals(2L, ((AuthProfile) during.getPrincipal()).getUserId());
			return 42L;
		});

		assertEquals(42L, result);
		Authentication after = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(after);
		assertEquals(1L, ((AuthProfile) after.getPrincipal()).getUserId());
	}

}
