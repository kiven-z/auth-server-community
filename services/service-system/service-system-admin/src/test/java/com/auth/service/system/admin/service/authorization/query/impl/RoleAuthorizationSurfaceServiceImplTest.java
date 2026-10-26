package com.auth.service.system.admin.service.authorization.query.impl;

import com.auth.module.security.contract.api.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.MenuRoleBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.RolePermissionBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.vo.authorization.GrantSubjectItemRow;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
	@DisplayName("授权面摘要：汇总权限菜单与授权对象计数")
	void getAuthorizationSummaryReturnsCounts() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(new SysRoleEntity());
		when(rolePermissionBindingQueryMapper.countPermissionsByRoleId(ROLE_ID, null)).thenReturn(4L);
		when(menuRoleBindingQueryMapper.countMenusByRoleId(ROLE_ID, null)).thenReturn(3L);
		when(grantBindingQueryMapper.countSubjectsByRoleIdAndType(ROLE_ID, GrantTableSubjectType.USER.name(), null))
			.thenReturn(6L);
		when(grantBindingQueryMapper.countSubjectsByRoleIdAndType(ROLE_ID, GrantTableSubjectType.DEPT.name(), null))
			.thenReturn(2L);
		when(grantBindingQueryMapper.countSubjectsByRoleIdAndType(ROLE_ID, GrantTableSubjectType.POST.name(), null))
			.thenReturn(1L);

		RoleAuthorizationSummaryVO summary = service.getAuthorizationSummary(ROLE_ID);

		assertThat(summary.getPermissionCount()).isEqualTo(4L);
		assertThat(summary.getMenuCount()).isEqualTo(3L);
		assertThat(summary.getGrantUserCount()).isEqualTo(6L);
		assertThat(summary.getGrantDeptCount()).isEqualTo(2L);
		assertThat(summary.getGrantPostCount()).isEqualTo(1L);
		verify(rbacReferenceChecker).getExisting(ROLE_ID);
	}

	@Test
	@DisplayName("授权对象种子：按类型返回 id+label")
	void listGrantSubjectSeedsReturnsRows() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(new SysRoleEntity());
		GrantSubjectItemRow row = new GrantSubjectItemRow();
		row.setId(21L);
		row.setLabel("张三");
		when(grantBindingQueryMapper.selectSubjectsByRoleIdAndType(ROLE_ID, GrantTableSubjectType.USER.name()))
			.thenReturn(List.of(row));

		List<GrantSubjectItemRow> seeds = service.listGrantSubjectSeeds(ROLE_ID, GrantTableSubjectType.USER);

		assertThat(seeds).hasSize(1);
		assertThat(seeds.get(0).getId()).isEqualTo(21L);
		assertThat(seeds.get(0).getLabel()).isEqualTo("张三");
		verify(grantBindingQueryMapper).selectSubjectsByRoleIdAndType(ROLE_ID, GrantTableSubjectType.USER.name());
	}

}
