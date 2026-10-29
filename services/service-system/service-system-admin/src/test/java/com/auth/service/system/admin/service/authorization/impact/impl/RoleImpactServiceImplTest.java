package com.auth.service.system.admin.service.authorization.impact.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.authorization.RoleImpactQueryMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.po.reference.UserReferencePO;
import com.auth.service.system.admin.model.query.authorization.RoleImpactUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.RoleImpactRefreshResultVO;
import com.auth.service.system.admin.model.vo.reference.UserReferenceVO;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserProfileRefreshTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link RoleImpactServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RoleImpactServiceImpl 角色影响面")
@ExtendWith(MockitoExtension.class)
class RoleImpactServiceImplTest {

	private static final Long ROLE_ID = 9L;

	@Mock
	private RbacReferenceChecker rbacReferenceChecker;

	@Mock
	private RoleImpactQueryMapper roleImpactQueryMapper;

	@Mock
	private UserProfileRefreshTrigger userProfileRefreshTrigger;

	private RoleImpactServiceImpl service;

	private static SysRoleEntity role() {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(ROLE_ID);
		entity.setRoleCode("OPS");
		return entity;
	}

	@BeforeEach
	void setUp() {
		service = new RoleImpactServiceImpl(rbacReferenceChecker, roleImpactQueryMapper, userProfileRefreshTrigger);
	}

	@Test
	@DisplayName("影响用户分页：显式 count 并转换 VO")
	void pageImpactUsersReturnsPagedRows() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(role());
		RoleImpactUserPageQuery query = new RoleImpactUserPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		when(roleImpactQueryMapper.countImpactUsersByRoleId(ROLE_ID, query)).thenReturn(1L);

		UserReferencePO po = new UserReferencePO();
		po.setId(7L);
		po.setUsername("alice");
		po.setNickname("爱丽丝");
		po.setStatus(1);
		Page<UserReferencePO> page = new Page<>(1, 10, 1, false);
		page.setRecords(List.of(po));
		when(roleImpactQueryMapper.selectImpactUsersByRoleIdPage(any(), eq(ROLE_ID), eq(query))).thenReturn(page);

		PageResponse<UserReferenceVO> result = service.pageImpactUsers(ROLE_ID, query);

		assertThat(result.getTotal()).isEqualTo(1L);
		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getUsername()).isEqualTo("alice");

		ArgumentCaptor<Page<?>> pageCaptor = ArgumentCaptor.captor();
		verify(roleImpactQueryMapper).selectImpactUsersByRoleIdPage(pageCaptor.capture(), eq(ROLE_ID), eq(query));
		assertThat(pageCaptor.getValue().searchCount()).isFalse();
		assertThat(pageCaptor.getValue().getTotal()).isEqualTo(1L);
	}

	@Test
	@DisplayName("刷新：无影响用户时不触发画像刷新")
	void refreshSkipsTriggerWhenNoImpactUsers() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(role());
		when(roleImpactQueryMapper.selectImpactUserIdsByRoleId(ROLE_ID)).thenReturn(List.of());

		RoleImpactRefreshResultVO result = service.refreshImpactedProfiles(ROLE_ID, null);

		assertThat(result.getRefreshedUserCount()).isZero();
		verify(userProfileRefreshTrigger, never()).refreshByUserIds(any());
	}

	@Test
	@DisplayName("刷新：指定 ID 与影响面取交集后触发画像刷新")
	void refreshIntersectsRequestedUserIds() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(role());
		when(roleImpactQueryMapper.selectImpactUserIdsByRoleId(ROLE_ID)).thenReturn(List.of(1L, 2L, 3L));

		RoleImpactRefreshResultVO result = service.refreshImpactedProfiles(ROLE_ID, List.of(2L, 9L));

		assertThat(result.getRefreshedUserCount()).isEqualTo(1L);
		verify(userProfileRefreshTrigger).refreshByUserIds(List.of(2L));
	}

	@Test
	@DisplayName("刷新：触发器失败时向上抛出 SERVICE_UNAVAILABLE")
	void refreshPropagatesTriggerFailure() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(role());
		when(roleImpactQueryMapper.selectImpactUserIdsByRoleId(ROLE_ID)).thenReturn(List.of(1L));
		doThrow(new SystemBusinessException(SystemCommonResultCode.SERVICE_UNAVAILABLE)).when(userProfileRefreshTrigger)
			.refreshByUserIds(List.of(1L));

		assertThatThrownBy(() -> service.refreshImpactedProfiles(ROLE_ID, null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.SERVICE_UNAVAILABLE);
	}

}
