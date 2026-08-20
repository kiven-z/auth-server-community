package com.auth.service.auth.service;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;
import com.auth.service.auth.service.impl.AdminAuthorizationServiceImpl;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminAuthorizationService} 单元测试
 */
@DisplayName("AdminAuthorizationService 管理端授权查询")
@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceTest {

	@Mock
	private AuthProfileRepository authProfileRepository;

	@InjectMocks
	private AdminAuthorizationServiceImpl adminAuthorizationService;

	@Test
	@DisplayName("用户不存在时返回空角色码与权限码")
	void getEffectiveCodes_missingUser_shouldReturnEmpty() {
		when(authProfileRepository.buildByUserIds(List.of(9L))).thenReturn(List.of());

		EffectiveCodesVO vo = adminAuthorizationService.getEffectiveCodes(9L);

		assertEquals(List.of(), vo.getRoleCodes());
		assertEquals(List.of(), vo.getPermissionCodes());
		verify(authProfileRepository).buildByUserIds(List.of(9L));
	}

	@Test
	@DisplayName("存在画像时映射角色码与权限码")
	void getEffectiveCodes_existingUser_shouldMapCodes() {
		AuthProfile profile = AuthProfile.builder()
			.userId(1L)
			.username("u1")
			.roles(List.of("ADMIN"))
			.permissions(List.of("sys:user:query"))
			.build();
		when(authProfileRepository.buildByUserIds(List.of(1L))).thenReturn(List.of(profile));

		EffectiveCodesVO vo = adminAuthorizationService.getEffectiveCodes(1L);

		assertEquals(List.of("ADMIN"), vo.getRoleCodes());
		assertEquals(List.of("sys:user:query"), vo.getPermissionCodes());
	}

}
