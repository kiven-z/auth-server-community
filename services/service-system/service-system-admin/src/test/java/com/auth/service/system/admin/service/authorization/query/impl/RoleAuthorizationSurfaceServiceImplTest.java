package com.auth.service.system.admin.service.authorization.query.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.MenuRoleBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.RolePermissionBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RoleAuthorizationSurfaceServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RoleAuthorizationSurfaceServiceImpl 角色授权面")
@ExtendWith(MockitoExtension.class)
class RoleAuthorizationSurfaceServiceImplTest {

	private static final Long ROLE_ID = 11L;

	@Mock
	private RbacReferenceChecker rbacReferenceChecker;

	@Mock
	private RolePermissionBindingQueryMapper rolePermissionBindingQueryMapper;

	@Mock
	private MenuRoleBindingQueryMapper menuRoleBindingQueryMapper;

	@Mock
	private GrantBindingQueryMapper grantBindingQueryMapper;

	private RoleAuthorizationSurfaceServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new RoleAuthorizationSurfaceServiceImpl(rbacReferenceChecker, rolePermissionBindingQueryMapper,
				menuRoleBindingQueryMapper, grantBindingQueryMapper);
	}

	@Test
	@DisplayName("授权面摘要：汇总权限菜单与授权用户计数")
	void getAuthorizationSummaryReturnsCounts() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(new SysRoleEntity());
		when(rolePermissionBindingQueryMapper.countPermissionsByRoleId(ROLE_ID, null)).thenReturn(4L);
		when(menuRoleBindingQueryMapper.countMenusByRoleId(ROLE_ID, null)).thenReturn(3L);
		when(grantBindingQueryMapper.countSubjectsByRoleIdAndType(ROLE_ID, GrantTableSubjectType.USER.name()))
			.thenReturn(6L);

		RoleAuthorizationSummaryVO summary = service.getAuthorizationSummary(ROLE_ID);

		assertThat(summary.getPermissionCount()).isEqualTo(4L);
		assertThat(summary.getMenuCount()).isEqualTo(3L);
		assertThat(summary.getGrantUserCount()).isEqualTo(6L);
		verify(rbacReferenceChecker).getExisting(ROLE_ID);
	}

}
