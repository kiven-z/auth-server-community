package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.admin.menu.SysMenuMapper;
import com.auth.service.system.admin.mapper.admin.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.mapper.authorization.MenuRoleBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.menu.MenuReferenceChecker;
import com.auth.service.system.admin.support.menu.MenuWebRouteTreeAssembler;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesResolver;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysMenuServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysMenuServiceImpl 系统菜单")
@ExtendWith(MockitoExtension.class)
class SysMenuServiceImplTest {

	@Mock
	private SysMenuMapper sysMenuMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private UserEffectiveCodesResolver userEffectiveCodesResolver;

	@Mock
	private MenuWebRouteTreeAssembler menuWebRouteTreeAssembler;

	@Mock
	private MenuReferenceChecker menuReferenceChecker;

	@Mock
	private SysMenuRoleMapper sysMenuRoleMapper;

	@Mock
	private MenuRoleBindingQueryMapper menuRoleBindingQueryMapper;

	@Mock
	private RbacReferenceChecker rbacReferenceChecker;

	private SysMenuServiceImpl sysMenuService;

	private static SysMenuEntity menuEntity(Long id) {
		SysMenuEntity entity = new SysMenuEntity();
		entity.setId(id);
		entity.setParentId(0L);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysMenuService = new SysMenuServiceImpl(auditUserDisplayService, userEffectiveCodesResolver,
				menuWebRouteTreeAssembler, menuReferenceChecker, sysMenuRoleMapper, menuRoleBindingQueryMapper,
				rbacReferenceChecker);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysMenuService, sysMenuMapper);
	}

	@Test
	@DisplayName("删除菜单：空列表直接返回")
	void deleteIgnoresEmptyIds() {
		sysMenuService.deleteByIds(List.of());

		verifyNoInteractions(sysMenuMapper);
	}

	@Test
	@DisplayName("删除菜单：存在未一并删除的子菜单时抛出 MENU_HAS_CHILDREN")
	void deleteThrowsWhenHasUndeletedChildren() {
		when(sysMenuMapper.selectFirstBlockedParentId(List.of(2L))).thenReturn(2L);
		List<Long> menuIds = List.of(2L);

		assertThatThrownBy(() -> sysMenuService.deleteByIds(menuIds)).isInstanceOf(SystemBusinessException.class)
			.satisfies(ex -> {
				SystemBusinessException biz = (SystemBusinessException) ex;
				assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.TREE_HAS_ACTIVE_CHILDREN);
				assertThat(biz.getMessageArgs()).isEmpty();
			});

		verify(sysMenuMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("移动菜单：父级未变时跳过更新")
	void moveSkipsUpdateWhenParentUnchanged() {
		SysMenuEntity existing = menuEntity(6L);
		when(menuReferenceChecker.getExisting(6L)).thenReturn(existing);
		when(menuReferenceChecker.normalizeAndRequireParent(0L)).thenReturn(0L);

		SysMenuMoveForm form = new SysMenuMoveForm();
		form.setParentId(0L);
		sysMenuService.move(6L, form);

		verify(sysMenuMapper, never()).updateById(any(SysMenuEntity.class));
	}

	@Test
	@DisplayName("移动菜单：父级变更时更新 parentId")
	void moveUpdatesParentWhenChanged() {
		SysMenuEntity existing = menuEntity(7L);
		when(menuReferenceChecker.getExisting(7L)).thenReturn(existing);
		when(menuReferenceChecker.normalizeAndRequireParent(12L)).thenReturn(12L);
		when(sysMenuMapper.updateById(any(SysMenuEntity.class))).thenReturn(1);

		SysMenuMoveForm form = new SysMenuMoveForm();
		form.setParentId(12L);
		sysMenuService.move(7L, form);

		verify(menuReferenceChecker).requireMoveTargetValid(7L, 12L);
		verify(sysMenuMapper).updateById(argThat((SysMenuEntity e) -> Long.valueOf(12L).equals(e.getParentId())));
	}

	@Test
	@DisplayName("删除菜单：无子节点冲突时批量物理删除")
	void deleteShouldBatchRemoveByIds() {
		when(sysMenuMapper.selectFirstBlockedParentId(List.of(3L, 3L, 4L))).thenReturn(null);

		sysMenuService.deleteByIds(Arrays.asList(3L, 3L, null, 4L));

		verify(sysMenuMapper).selectFirstBlockedParentId(List.of(3L, 3L, 4L));
		verify(sysMenuMapper).deleteByIds(anyCollection());
	}

	@Test
	@DisplayName("菜单详情：不存在时抛出 DATA_NOT_EXIST")
	void getDetailThrowsWhenNotFound() {
		when(menuReferenceChecker.getExisting(9L))
			.thenThrow(new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST));

		assertThatThrownBy(() -> sysMenuService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("菜单详情：填充已绑定角色计数")
	void getDetailShouldIncludeBoundRoles() {
		SysMenuEntity entity = menuEntity(5L);
		entity.setName("SystemUser");
		entity.setTitle("用户管理");
		when(menuReferenceChecker.getExisting(5L)).thenReturn(entity);
		when(menuRoleBindingQueryMapper.countRolesByMenuId(5L, null)).thenReturn(1L);

		SysMenuDetailVO detail = sysMenuService.getDetail(5L);

		assertThat(detail.getName()).isEqualTo("SystemUser");
		assertThat(detail.getBoundRoleCount()).isEqualTo(1L);
		verify(menuRoleBindingQueryMapper).countRolesByMenuId(5L, null);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("覆盖分配：校验通过后删除旧关联并批量插入")
	void replaceMenuRolesReplacesAssociationsWhenEligible() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleIds(List.of(1L, 2L));

		sysMenuService.replaceMenuRoles(menuId, form);

		verify(rbacReferenceChecker).requireExistingEnabledRoleIds(List.of(1L, 2L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(sysMenuRoleMapper).deleteByMenuId(menuId);
		verify(sysMenuRoleMapper).batchInsertMenuRoles(menuId, List.of(1L, 2L));
	}

	@Test
	@DisplayName("覆盖分配：空列表仅删除关联不插入")
	void replaceMenuRolesWithEmptyListDeletesOnly() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleIds(List.of());

		sysMenuService.replaceMenuRoles(menuId, form);

		verify(rbacReferenceChecker).requireExistingEnabledRoleIds(List.of(),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(sysMenuRoleMapper).deleteByMenuId(menuId);
		verify(sysMenuRoleMapper, never()).batchInsertMenuRoles(any(), any());
	}

	@Test
	@DisplayName("覆盖分配：角色校验失败时不写 sys_menu_role")
	void replaceMenuRolesThrowsWhenRoleInvalid() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleIds(List.of(99L));

		doThrow(new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID)).when(rbacReferenceChecker)
			.requireExistingEnabledRoleIds(List.of(99L), SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		assertThatThrownBy(() -> sysMenuService.replaceMenuRoles(menuId, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		verify(sysMenuRoleMapper, never()).deleteByMenuId(any());
		verify(sysMenuRoleMapper, never()).batchInsertMenuRoles(any(), any());
	}

}
