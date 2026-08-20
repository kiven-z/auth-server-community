package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserRoleServiceImpl} 单元测试
 */
@DisplayName("SysUserRoleServiceImpl 用户直连角色授权")
@ExtendWith(MockitoExtension.class)
class SysUserRoleServiceImplTest {

	private static final Long USER_ID = 10L;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private RbacReferenceChecker rbacReferenceChecker;

	@Mock
	private GrantTableService grantTableService;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private SysUserRoleServiceImpl sysUserRoleService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SysUserRoleServiceImplTest.USER_ID);
		entity.setUsername("tester");
		return entity;
	}

	@BeforeEach
	void setUp() {
		sysUserRoleService = new SysUserRoleServiceImpl(userReferenceChecker, rbacReferenceChecker, grantTableService,
				userAuthorizationInvalidationTrigger);
	}

	@Test
	@DisplayName("查询已分配角色：按 USER 主体委托 grant_table")
	void listAssignedRolesDelegatesToGrantTableAsUserSubject() {
		RoleReferenceVO boundRole = new RoleReferenceVO();
		boundRole.setId(101L);
		boundRole.setRoleName("业务角色A");
		when(grantTableService.listAssignedRoles(GrantTableSubjectType.USER, USER_ID)).thenReturn(List.of(boundRole));

		List<RoleReferenceVO> rows = sysUserRoleService.listAssignedRoles(USER_ID);

		assertThat(rows).containsExactly(boundRole);
		verify(grantTableService).listAssignedRoles(GrantTableSubjectType.USER, USER_ID);
		verify(grantTableService, never()).replaceSubjectRoleGrants(any(), any(), any());
	}

	@Test
	@DisplayName("查询已分配角色：无授权时返回空列表")
	void listAssignedRolesReturnsEmptyWhenNoneBound() {
		when(grantTableService.listAssignedRoles(GrantTableSubjectType.USER, USER_ID)).thenReturn(List.of());

		assertThat(sysUserRoleService.listAssignedRoles(USER_ID)).isEmpty();
	}

	@Test
	@DisplayName("全量覆盖：写入 grant_table 并提交用户授权失效")
	void replaceUserRolesWritesGrantAndSubmitsInvalidation() {
		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(activeUser());
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleIds(List.of(101L, 102L));

		sysUserRoleService.replaceUserRoles(USER_ID, form);

		verify(userReferenceChecker).requireOperable(List.of(USER_ID));
		verify(rbacReferenceChecker).requireExistingEnabledRoleIds(List.of(101L, 102L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(grantTableService).replaceSubjectRoleGrants(GrantTableSubjectType.USER.name(), USER_ID,
				List.of(101L, 102L));
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "replace-roles");
	}

	@Test
	@DisplayName("全量覆盖：空角色列表仅删除 grant 行仍触发用户失效")
	void replaceUserRolesClearsGrantsWhenRoleCodesEmpty() {
		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(activeUser());
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleIds(List.of());

		sysUserRoleService.replaceUserRoles(USER_ID, form);

		verify(grantTableService).replaceSubjectRoleGrants(GrantTableSubjectType.USER.name(), USER_ID, List.of());
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "replace-roles");
	}

	@Test
	@DisplayName("全量覆盖：受保护用户不允许分配角色")
	void replaceUserRolesThrowsWhenUserNotOperable() {
		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(activeUser());
		doThrow(new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN)).when(userReferenceChecker)
			.requireOperable(List.of(USER_ID));
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleIds(List.of(101L));

		assertThatThrownBy(() -> sysUserRoleService.replaceUserRoles(USER_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

		verify(grantTableService, never()).replaceSubjectRoleGrants(any(), any(), any());
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

}
