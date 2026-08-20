package com.auth.service.system.authorization.dispatch.query;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.feign.AuthorizationInternalFeignClient;
import com.auth.service.system.authorization.feign.dto.EffectiveCodesInnerDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link UserEffectiveCodesResolver} 单元测试
 */
@DisplayName("UserEffectiveCodesResolver 生效码查询")
@ExtendWith(MockitoExtension.class)
class UserEffectiveCodesResolverTest {

	@Mock
	private AuthorizationInternalFeignClient authorizationInternalFeignClient;

	@InjectMocks
	private UserEffectiveCodesResolver userEffectiveCodesResolver;

	@Test
	@DisplayName("userId 为 null 时返回空且不调用 Feign")
	void resolve_whenUserIdNull_shouldReturnEmpty() {
		Optional<UserEffectiveCodesSnapshot> snapshot = userEffectiveCodesResolver.resolve(null);

		assertTrue(snapshot.isEmpty());
		verifyNoInteractions(authorizationInternalFeignClient);
	}

	@Test
	@DisplayName("Feign 成功时映射角色码与权限码")
	void resolve_whenFeignSuccess_shouldMapSnapshot() {
		EffectiveCodesInnerDTO dto = new EffectiveCodesInnerDTO();
		dto.setRoleCodes(List.of("ADMIN"));
		dto.setPermissionCodes(List.of("sys:user:query"));
		when(authorizationInternalFeignClient.getEffectiveCodes(2L)).thenReturn(Result.success(dto));

		UserEffectiveCodesSnapshot snapshot = userEffectiveCodesResolver.resolve(2L).orElseThrow();

		assertEquals(List.of("ADMIN"), snapshot.roleCodes());
		assertEquals(List.of("sys:user:query"), snapshot.permissionCodes());
	}

	@Test
	@DisplayName("Feign 失败时返回空")
	void resolve_whenFeignFails_shouldReturnEmpty() {
		when(authorizationInternalFeignClient.getEffectiveCodes(3L)).thenReturn(Result.error("unavailable"));

		Optional<UserEffectiveCodesSnapshot> snapshot = userEffectiveCodesResolver.resolve(3L);

		assertTrue(snapshot.isEmpty());
	}

}
