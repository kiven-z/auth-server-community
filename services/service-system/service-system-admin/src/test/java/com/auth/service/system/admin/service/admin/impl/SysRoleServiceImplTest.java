package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.service.authorization.query.RoleAuthorizationSurfaceService;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysRoleServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysRoleServiceImpl 系统角色")
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private RoleAuthorizationSurfaceService roleAuthorizationSurfaceService;

	private SysRoleServiceImpl sysRoleService;

	private static SysRoleForm updateForm(Long id, String roleCode, boolean status) {
		SysRoleForm form = new SysRoleForm();
		form.setId(id);
		form.setRoleCode(roleCode);
		form.setRoleName("测试角色");
		form.setStatus(status);
		form.setOrderNum(0);
		return form;
	}

	private static SysRoleEntity roleEntity(Long id, String roleCode) {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(id);
		entity.setRoleCode(roleCode);
		entity.setRoleName("测试角色");
		entity.setStatus(true);
		return entity;
	}

	private static RoleAuthorizationSummaryVO summary() {
		RoleAuthorizationSummaryVO value = new RoleAuthorizationSummaryVO();
		value.setPermissionCount(3L);
		value.setMenuCount(2L);
		value.setGrantUserCount(5L);
		return value;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysRoleService = spy(new SysRoleServiceImpl(auditUserDisplayService, roleInvalidationTrigger, grantTableMapper,
				roleAuthorizationSurfaceService));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
		lenient().doReturn(true).when(sysRoleService).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增角色：空列表直接返回")
	void createBatchFromImportSkipsWhenEmpty() {
		sysRoleService.createBatchFromImport(List.of());

		verify(sysRoleMapper, never()).selectReferenceByRoleCodes(anyList());
		verify(sysRoleService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增角色：校验通过后批量落库")
	void createBatchFromImportPersistsEntities() {
		SysRoleForm form = new SysRoleForm();
		form.setRoleCode("AUDITOR");
		form.setRoleName("审计员");
		form.setStatus(true);
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("AUDITOR"))).thenReturn(List.of());

		sysRoleService.createBatchFromImport(List.of(form));

		verify(sysRoleMapper).selectReferenceByRoleCodes(List.of("AUDITOR"));
		verify(sysRoleService).saveBatch(anyList(), eq(BatchSizes.SIZE_500));
	}

	@Test
	@DisplayName("批量新增角色：请求内编码重复时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenDuplicatedInRequest() {
		SysRoleForm form1 = new SysRoleForm();
		form1.setRoleCode("ADMIN");
		form1.setRoleName("管理员");
		SysRoleForm form2 = new SysRoleForm();
		form2.setRoleCode("ADMIN");
		form2.setRoleName("管理员2");

		ThrowingCallable executable = () -> sysRoleService.createBatchFromImport(List.of(form1, form2));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysRoleMapper, never()).selectReferenceByRoleCodes(anyList());
		verify(sysRoleService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增角色：编码已存在时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenRoleCodeExists() {
		SysRoleForm form = new SysRoleForm();
		form.setRoleCode("ADMIN");
		form.setRoleName("管理员");
		RoleReferencePO existing = new RoleReferencePO();
		existing.setRoleCode("ADMIN");
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("ADMIN"))).thenReturn(List.of(existing));

		ThrowingCallable executable = () -> sysRoleService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysRoleService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("角色详情：不存在时抛出 ROLE_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysRoleMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(roleAuthorizationSurfaceService);
	}

	@Test
	@DisplayName("角色详情：复用授权面摘要填充计数")
	void getDetailShouldIncludeAuthorizationCounts() {
		when(sysRoleMapper.selectById(1L)).thenReturn(roleEntity(1L, "ADMIN"));
		when(roleAuthorizationSurfaceService.getAuthorizationSummary(1L)).thenReturn(summary());

		SysRoleDetailVO detail = sysRoleService.getDetail(1L);

		assertThat(detail.getRoleCode()).isEqualTo("ADMIN");
		assertThat(detail.getPermissionCount()).isEqualTo(3L);
		assertThat(detail.getMenuCount()).isEqualTo(2L);
		assertThat(detail.getGrantUserCount()).isEqualTo(5L);
		verify(roleAuthorizationSurfaceService).getAuthorizationSummary(1L);
		verifyNoInteractions(grantTableMapper);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("更新角色：仅改名称时不提交授权失效")
	void updateWithoutAuthImpactSkipsInvalidation() {
		when(sysRoleMapper.selectById(4L)).thenReturn(roleEntity(4L, "EDITOR"));
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		SysRoleForm form = updateForm(4L, "EDITOR", true);
		form.setRoleName("编辑者");

		sysRoleService.update(form);

		verify(sysRoleMapper).updateById(argThat((SysRoleEntity e) -> "编辑者".equals(e.getRoleName())));
		verifyNoInteractions(roleInvalidationTrigger);
	}

	@Test
	@DisplayName("更新角色：状态变更时提交 ROLE 授权失效")
	void updateWithStatusChangeSubmitsInvalidation() {
		when(sysRoleMapper.selectById(5L)).thenReturn(roleEntity(5L, "VIEWER"));
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		sysRoleService.update(updateForm(5L, "VIEWER", false));

		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("VIEWER"), "update");
	}

	@Test
	@DisplayName("更新角色：编码重命名时提交失效并携带旧新码")
	void updateWithRoleCodeRenameSubmitsInvalidation() {
		when(sysRoleMapper.selectById(6L)).thenReturn(roleEntity(6L, "OLD_CODE"));
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		sysRoleService.update(updateForm(6L, "NEW_CODE", true));

		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("OLD_CODE", "NEW_CODE"), "update");
	}

	@Test
	@DisplayName("删除角色：不存在时抛出 ROLE_NOT_FOUND")
	void deleteThrowsWhenNotFound() {
		when(sysRoleMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verify(sysRoleMapper, never()).deleteById(any());
		verifyNoInteractions(roleInvalidationTrigger);
	}

	@Test
	@DisplayName("删除角色：grant_table 引用存在时抛出 ROLE_IN_USE")
	void deleteThrowsWhenGrantInUse() {
		when(sysRoleMapper.selectById(2L)).thenReturn(roleEntity(2L, "CUSTOM"));
		when(grantTableMapper.countByRoleId(2L)).thenReturn(1L);

		assertThatThrownBy(() -> sysRoleService.deleteById(2L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(sysRoleMapper, never()).deleteById(any());
		verifyNoInteractions(roleInvalidationTrigger);
	}

	@Test
	@DisplayName("删除角色：物理删除主表并触发授权失效（子表由数据库 CASCADE 维护）")
	void deleteShouldPhysicallyDeleteAndInvalidate() {
		when(sysRoleMapper.selectById(3L)).thenReturn(roleEntity(3L, "EMPTY_ROLE"));
		when(grantTableMapper.countByRoleId(3L)).thenReturn(0L);
		when(sysRoleMapper.deleteById(3L)).thenReturn(1);

		sysRoleService.deleteById(3L);

		verify(sysRoleMapper).deleteById(3L);
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("EMPTY_ROLE"), "delete");
	}

	@Test
	@DisplayName("批量启停：按存在角色更新并按编码失效")
	void batchUpdateStatusUpdatesAndInvalidatesByRoleCodes() {
		doReturn(List.of(roleEntity(1L, "R1"), roleEntity(2L, "R2"))).when(sysRoleService).listByIds(any());
		doReturn(true).when(sysRoleService).updateBatchById(anyList());

		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(false);
		sysRoleService.batchUpdateStatus(form);

		verify(sysRoleService).updateBatchById(anyList());
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("R1", "R2"), "update");
	}

	@Test
	@DisplayName("批量启停：ids 为空时不写库")
	void batchUpdateStatusSkipsWhenIdsEmpty() {
		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of());
		form.setStatus(true);
		sysRoleService.batchUpdateStatus(form);

		verify(sysRoleService, never()).listByIds(any());
		verify(sysRoleService, never()).updateBatchById(anyList());
		verifyNoInteractions(roleInvalidationTrigger);
	}

}
