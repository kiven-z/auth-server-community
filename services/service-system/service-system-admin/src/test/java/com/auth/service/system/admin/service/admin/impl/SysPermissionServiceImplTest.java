package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.admin.role.SysRolePermissionMapper;
import com.auth.service.system.admin.mapper.authorization.RolePermissionBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.authorization.dispatch.trigger.PermissionAuthorizationInvalidationTrigger;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

/**
 * {@link SysPermissionServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysPermissionServiceImpl 系统权限")
@ExtendWith(MockitoExtension.class)
class SysPermissionServiceImplTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger;

	@Mock
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	@Mock
	private SysRolePermissionMapper sysRolePermissionMapper;

	@Mock
	private RolePermissionBindingQueryMapper rolePermissionBindingQueryMapper;

	private SysPermissionServiceImpl sysPermissionService;

	private static SysPermissionEntity permissionEntity(Long id) {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(id);
		entity.setPermissionCode("sys:user:query");
		entity.setPermissionName("测试权限");
		entity.setStatus(true);
		entity.setOrderNum(0);
		return entity;
	}

	private static SysPermissionForm updateForm(Long id, String permissionCode, Boolean status) {
		SysPermissionForm form = new SysPermissionForm();
		form.setId(id);
		form.setPermissionCode(permissionCode);
		form.setPermissionName("测试权限");
		form.setStatus(status);
		form.setOrderNum(0);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysPermissionService = spy(new SysPermissionServiceImpl(auditUserDisplayService, permissionInvalidationTrigger,
				roleInvalidationTrigger, sysRolePermissionMapper, rolePermissionBindingQueryMapper));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPermissionService, sysPermissionMapper);
		lenient().doReturn(true).when(sysPermissionService).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增权限：空列表直接返回")
	void createBatchFromImportSkipsWhenEmpty() {
		sysPermissionService.createBatchFromImport(List.of());

		verify(sysPermissionMapper, never()).selectReferenceByPermissionCodes(anyList());
		verify(sysPermissionService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增权限：校验通过后批量落库")
	void createBatchFromImportPersistsEntities() {
		SysPermissionForm form = new SysPermissionForm();
		form.setPermissionCode("sys:user:list");
		form.setPermissionName("列表");
		form.setStatus(true);
		when(sysPermissionMapper.selectReferenceByPermissionCodes(List.of("sys:user:list"))).thenReturn(List.of());

		sysPermissionService.createBatchFromImport(List.of(form));

		verify(sysPermissionMapper).selectReferenceByPermissionCodes(List.of("sys:user:list"));
		verify(sysPermissionService).saveBatch(anyList(), eq(BatchSizes.SIZE_500));
	}

	@Test
	@DisplayName("批量新增权限：请求内编码重复时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenDuplicatedInRequest() {
		SysPermissionForm form1 = new SysPermissionForm();
		form1.setPermissionCode("sys:user:query");
		form1.setPermissionName("查询");
		SysPermissionForm form2 = new SysPermissionForm();
		form2.setPermissionCode("sys:user:query");
		form2.setPermissionName("查询2");

		ThrowingCallable executable = () -> sysPermissionService.createBatchFromImport(List.of(form1, form2));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysPermissionMapper, never()).selectReferenceByPermissionCodes(anyList());
		verify(sysPermissionService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增权限：编码已存在时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenPermissionCodeExists() {
		SysPermissionForm form = new SysPermissionForm();
		form.setPermissionCode("sys:user:list");
		form.setPermissionName("列表");
		PermissionReferencePO existing = new PermissionReferencePO();
		existing.setPermissionCode("sys:user:list");
		when(sysPermissionMapper.selectReferenceByPermissionCodes(List.of("sys:user:list")))
			.thenReturn(List.of(existing));

		ThrowingCallable executable = () -> sysPermissionService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysPermissionService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("更新权限：不存在时抛出 PERMISSION_NOT_FOUND")
	void updateThrowsWhenNotFound() {
		SysPermissionForm form = updateForm(9L, "sys:user:query", true);
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(permissionInvalidationTrigger);
	}

	@Test
	@DisplayName("更新权限：编码冲突时抛出 PERMISSION_CODE_DUPLICATE")
	void updateThrowsWhenPermissionCodeDuplicate() {
		SysPermissionForm form = updateForm(1L, "sys:user:update", true);
		when(sysPermissionMapper.selectById(1L)).thenReturn(permissionEntity(1L));
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysPermissionService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verifyNoInteractions(permissionInvalidationTrigger);
	}

	@Test
	@DisplayName("更新权限：仅名称变更时不触发授权失效")
	void updateShouldNotInvalidateWhenOnlyNameChanged() {
		SysPermissionForm form = updateForm(2L, "sys:user:query", true);
		form.setPermissionName("用户查询");
		when(sysPermissionMapper.selectById(2L)).thenReturn(permissionEntity(2L));

		sysPermissionService.update(form);

		verify(permissionInvalidationTrigger, never()).submitByPermissionCodes(any(), any());
	}

	@Test
	@DisplayName("更新权限：状态变更时触发授权失效")
	void updateShouldInvalidateWhenStatusChanged() {
		SysPermissionForm form = updateForm(3L, "sys:user:query", false);
		when(sysPermissionMapper.selectById(3L)).thenReturn(permissionEntity(3L));

		sysPermissionService.update(form);

		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:user:query"), "update");
	}

	@Test
	@DisplayName("更新权限：编码重命名时触发授权失效并携带旧新码")
	void updateShouldInvalidateWhenPermissionCodeRenamed() {
		SysPermissionForm form = updateForm(4L, "sys:user:list", true);
		when(sysPermissionMapper.selectById(4L)).thenReturn(permissionEntity(4L));
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);

		sysPermissionService.update(form);

		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:user:query", "sys:user:list"),
				"update");
	}

	@Test
	@DisplayName("权限详情：不存在时抛出 PERMISSION_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("权限详情：返回 VO 并填充审计用户名")
	void getDetailReturnsVoAndEnrichesAuditUsernames() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(3L);
		entity.setPermissionCode("sys:dept:query");
		entity.setPermissionName("部门查询");
		entity.setStatus(true);
		when(sysPermissionMapper.selectById(3L)).thenReturn(entity);
		when(rolePermissionBindingQueryMapper.countRolesByPermissionId(3L, null)).thenReturn(0L);

		SysPermissionDetailVO detail = sysPermissionService.getDetail(3L);

		assertThat(detail.getPermissionCode()).isEqualTo("sys:dept:query");
		assertThat(detail.getBoundRoleCount()).isZero();
		verify(rolePermissionBindingQueryMapper).countRolesByPermissionId(3L, null);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("权限详情：按钮权限填充绑定角色计数")
	void getDetailShouldIncludeBoundRolesForButtonPermission() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(7L);
		entity.setPermissionCode("sys:user:create");
		entity.setPermissionName("新增用户");
		entity.setStatus(true);
		when(sysPermissionMapper.selectById(7L)).thenReturn(entity);
		when(rolePermissionBindingQueryMapper.countRolesByPermissionId(7L, null)).thenReturn(1L);

		SysPermissionDetailVO detail = sysPermissionService.getDetail(7L);

		assertThat(detail.getBoundRoleCount()).isEqualTo(1L);
		verify(rolePermissionBindingQueryMapper).countRolesByPermissionId(7L, null);
	}

	@Test
	@DisplayName("删除权限：不存在时抛出 PERMISSION_NOT_FOUND")
	void deleteThrowsWhenNotFound() {
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verify(sysPermissionMapper, never()).deleteById(any());
	}

	@Test
	@DisplayName("删除权限：删除前快照角色并提交 ROLE 失效，再物理删除")
	void deleteShouldInvalidateBoundRolesBeforePhysicalDelete() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(5L);
		entity.setPermissionCode("sys:role:delete");
		when(sysPermissionMapper.selectById(5L)).thenReturn(entity);
		when(sysRolePermissionMapper.selectRoleCodesByPermissionId(5L)).thenReturn(List.of("TEST"));

		sysPermissionService.deleteById(5L);

		InOrder order = Mockito.inOrder(permissionInvalidationTrigger, roleInvalidationTrigger, sysPermissionMapper);
		order.verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:role:delete"), "delete");
		order.verify(roleInvalidationTrigger).submitByRoleCodes(List.of("TEST"), "delete-permission");
		order.verify(sysPermissionMapper).deleteById(5L);
	}

	@Test
	@DisplayName("删除权限：无绑定角色时不提交 ROLE 失效")
	void deleteShouldSkipRoleInvalidationWhenNoBoundRoles() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(6L);
		entity.setPermissionCode("sys:orphan:perm");
		when(sysPermissionMapper.selectById(6L)).thenReturn(entity);
		when(sysRolePermissionMapper.selectRoleCodesByPermissionId(6L)).thenReturn(List.of());

		sysPermissionService.deleteById(6L);

		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:orphan:perm"), "delete");
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
		verify(sysPermissionMapper).deleteById(6L);
	}

	@Test
	@DisplayName("批量启停：按存在权限更新并按编码失效")
	void batchUpdateStatusUpdatesAndInvalidatesByPermissionCodes() {
		SysPermissionEntity p1 = permissionEntity(1L);
		p1.setPermissionCode("sys:a");
		SysPermissionEntity p2 = permissionEntity(2L);
		p2.setPermissionCode("sys:b");
		doReturn(List.of(p1, p2)).when(sysPermissionService).listByIds(any());
		doReturn(true).when(sysPermissionService).updateBatchById(anyList());

		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(false);
		sysPermissionService.batchUpdateStatus(form);

		verify(sysPermissionService).updateBatchById(anyList());
		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:a", "sys:b"), "update");
	}

	@Test
	@DisplayName("批量启停：ids 为空时不写库")
	void batchUpdateStatusSkipsWhenIdsEmpty() {
		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of());
		form.setStatus(true);
		sysPermissionService.batchUpdateStatus(form);

		verify(sysPermissionService, never()).listByIds(any());
		verify(sysPermissionService, never()).updateBatchById(anyList());
		verifyNoInteractions(permissionInvalidationTrigger);
	}

}
