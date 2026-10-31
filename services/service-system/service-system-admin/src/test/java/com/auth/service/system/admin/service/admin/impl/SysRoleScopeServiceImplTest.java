package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.admin.role.RoleScopeMapper;
import com.auth.service.system.admin.model.entity.RoleScopeEntity;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.role.SysRoleScopeVO;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.scope.DataScopeFormSupport;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SysRoleScopeServiceImpl} 单元测试
 */
@DisplayName("SysRoleScopeServiceImpl 角色数据范围")
@ExtendWith(MockitoExtension.class)
class SysRoleScopeServiceImplTest {

	@Mock
	private RbacReferenceChecker rbacReferenceChecker;

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	@Mock
	private RoleScopeMapper roleScopeMapper;

	private SysRoleScopeServiceImpl sysRoleScopeService;

	private static SysRoleEntity role(Long id, String code) {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(id);
		role.setRoleCode(code);
		return role;
	}

	private static SysDataScopeForm form(String dimension, String scopeType, List<Long> scopeIds, String remark) {
		SysDataScopeForm form = new SysDataScopeForm();
		form.setDimension(dimension);
		form.setScopeType(scopeType);
		form.setScopeIds(scopeIds);
		form.setRemark(remark);
		return form;
	}

	private static SysDeptEntity dept(Long id) {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(id);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		DataScopeFormSupport dataScopeFormSupport = new DataScopeFormSupport(sysDeptMapper);
		sysRoleScopeService = spy(
				new SysRoleScopeServiceImpl(rbacReferenceChecker, dataScopeFormSupport, roleInvalidationTrigger));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleScopeService, roleScopeMapper);
	}

	@Test
	@DisplayName("角色不存在时抛出 DATA_NOT_EXIST")
	void getByRoleIdWhenRoleMissingShouldThrow() {
		when(rbacReferenceChecker.getExisting(99L))
			.thenThrow(new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST));

		assertThatThrownBy(() -> sysRoleScopeService.getByRoleId(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("无 role_scope 行时返回空列表")
	void getByRoleIdWhenNoScopeRowShouldReturnEmpty() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role);
		when(roleScopeMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

		assertThat(sysRoleScopeService.getByRoleId(1L)).isEmpty();
	}

	@Test
	@DisplayName("有配置行时回显维度、类型与节点 ID 列表")
	void getByRoleIdShouldReturnScopeAndIds() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(3L);
		when(rbacReferenceChecker.getExisting(3L)).thenReturn(role);

		RoleScopeEntity entity = new RoleScopeEntity();
		entity.setId(100L);
		entity.setRoleId(3L);
		entity.setDimension("DEPT");
		entity.setScopeType("IDS_AND_CHILD");
		entity.setScopeIds("[110,120]");
		entity.setRemark("华北区");
		when(roleScopeMapper.selectList(any(Wrapper.class))).thenReturn(List.of(entity));

		List<SysRoleScopeVO> scopes = sysRoleScopeService.getByRoleId(3L);

		assertThat(scopes).hasSize(1);
		SysRoleScopeVO vo = scopes.get(0);
		assertThat(vo.getId()).isEqualTo(100L);
		assertThat(vo.getRoleId()).isEqualTo(3L);
		assertThat(vo.getDimension()).isEqualTo("DEPT");
		assertThat(vo.getScopeType()).isEqualTo("IDS_AND_CHILD");
		assertThat(vo.getScopeIds()).containsExactly(110L, 120L);
		assertThat(vo.getRemark()).isEqualTo("华北区");
	}

	@Test
	@DisplayName("scope_ids 为空时返回空节点列表")
	void getByRoleIdWhenIdsBlankShouldReturnEmptyList() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(2L);
		when(rbacReferenceChecker.getExisting(2L)).thenReturn(role);

		RoleScopeEntity entity = new RoleScopeEntity();
		entity.setId(200L);
		entity.setRoleId(2L);
		entity.setDimension("DEPT");
		entity.setScopeType("ALL");
		entity.setScopeIds(null);
		when(roleScopeMapper.selectList(any(Wrapper.class))).thenReturn(List.of(entity));

		List<SysRoleScopeVO> scopes = sysRoleScopeService.getByRoleId(2L);

		assertThat(scopes).hasSize(1);
		assertThat(scopes.get(0).getScopeType()).isEqualTo("ALL");
		assertThat(scopes.get(0).getScopeIds()).isEmpty();
	}

	@Test
	@DisplayName("replace：无行时删除零行后插入并触发一次失效")
	void replaceWhenMissingShouldInsertAndInvalidate() {
		SysRoleEntity role = role(1L, "ADMIN");
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role);
		when(roleScopeMapper.delete(any(Wrapper.class))).thenReturn(0);
		doReturn(true).when(sysRoleScopeService).saveBatch(anyList(), anyInt());

		SysDataScopeForm form = form("DEPT", "ALL", null, "全量");
		sysRoleScopeService.replace(1L, List.of(form));

		ArgumentCaptor<List<RoleScopeEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(roleScopeMapper).delete(any(Wrapper.class));
		verify(sysRoleScopeService).saveBatch(captor.capture(), eq(BatchSizes.SIZE_500));
		assertThat(captor.getValue()).hasSize(1);
		RoleScopeEntity entity = captor.getValue().get(0);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getRoleId()).isEqualTo(1L);
		assertThat(entity.getDimension()).isEqualTo("DEPT");
		assertThat(entity.getScopeType()).isEqualTo("ALL");
		assertThat(entity.getScopeIds()).isNull();
		assertThat(entity.getRemark()).isEqualTo("全量");
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "replace-role-scope");
	}

	@Test
	@DisplayName("replace：已有行时先删除再插入并触发一次失效")
	void replaceWhenExistsShouldDeleteThenInsert() {
		SysRoleEntity role = role(3L, "REGION_MGR_NORTH");
		when(rbacReferenceChecker.getExisting(3L)).thenReturn(role);
		when(roleScopeMapper.delete(any(Wrapper.class))).thenReturn(1);
		doReturn(true).when(sysRoleScopeService).saveBatch(anyList(), anyInt());

		SysDataScopeForm form = form("DEPT", "IDS_AND_CHILD", List.of(110L, 110L, 120L), "华北");
		when(sysDeptMapper.selectByIds(List.of(110L, 120L))).thenReturn(List.of(dept(110L), dept(120L)));
		sysRoleScopeService.replace(3L, List.of(form));

		verify(sysDeptMapper).selectByIds(List.of(110L, 120L));
		ArgumentCaptor<List<RoleScopeEntity>> captor = ArgumentCaptor.forClass(List.class);
		InOrder inOrder = inOrder(roleScopeMapper, sysRoleScopeService);
		inOrder.verify(roleScopeMapper).delete(any(Wrapper.class));
		inOrder.verify(sysRoleScopeService).saveBatch(captor.capture(), eq(BatchSizes.SIZE_500));
		RoleScopeEntity entity = captor.getValue().get(0);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getRoleId()).isEqualTo(3L);
		assertThat(entity.getDimension()).isEqualTo("DEPT");
		assertThat(entity.getScopeType()).isEqualTo("IDS_AND_CHILD");
		assertThat(entity.getScopeIds()).isEqualTo("[110,120]");
		assertThat(entity.getRemark()).isEqualTo("华北");
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("REGION_MGR_NORTH"), "replace-role-scope");
	}

	@Test
	@DisplayName("replace：同一维重复时抛出 PARAM_DUPLICATE")
	void replaceWhenDimensionDuplicatedShouldThrow() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));

		assertThatThrownBy(() -> sysRoleScopeService.replace(1L,
				List.of(form("DEPT", "ALL", null, null), form("DEPT", "SELF", null, null))))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);
		verify(roleScopeMapper, never()).delete(any());
		verify(sysRoleScopeService, never()).saveBatch(anyList(), anyInt());
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
	}

	@Test
	@DisplayName("replace：非法 scopeType 抛出 DATA_INVALID")
	void replaceWhenScopeTypeInvalidShouldThrow() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));

		SysDataScopeForm form = form("DEPT", "UNKNOWN", null, null);
		assertThatThrownBy(() -> sysRoleScopeService.replace(1L, List.of(form)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
		verify(roleScopeMapper, never()).delete(any());
		verify(sysRoleScopeService, never()).saveBatch(anyList(), anyInt());
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
	}

	@Test
	@DisplayName("replace：IDS 未传节点 ID 抛出 PARAM_REQUIRED")
	void replaceWhenIdsScopeMissingIdsShouldThrow() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));

		SysDataScopeForm form = form("DEPT", "IDS", List.of(), null);
		assertThatThrownBy(() -> sysRoleScopeService.replace(1L, List.of(form)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(sysDeptMapper, never()).selectByIds(any());
		verify(roleScopeMapper, never()).delete(any());
		verify(sysRoleScopeService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("replace：部门 ID 无效时抛出 GRANT_REFERENCE_INVALID")
	void replaceWhenDeptInvalidShouldThrow() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));
		when(sysDeptMapper.selectByIds(List.of(99L))).thenReturn(List.of());

		SysDataScopeForm form = form("DEPT", "IDS", List.of(99L), null);
		assertThatThrownBy(() -> sysRoleScopeService.replace(1L, List.of(form)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(roleScopeMapper, never()).delete(any());
		verify(sysRoleScopeService, never()).saveBatch(anyList(), anyInt());
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
	}

	@Test
	@DisplayName("replace：SELF 忽略表单中的节点 ID")
	void replaceSelfShouldClearIds() {
		SysRoleEntity role = role(6L, "DEPT_MGR");
		when(rbacReferenceChecker.getExisting(6L)).thenReturn(role);
		when(roleScopeMapper.delete(any(Wrapper.class))).thenReturn(0);
		doReturn(true).when(sysRoleScopeService).saveBatch(anyList(), anyInt());

		SysDataScopeForm form = form("DEPT", "SELF", List.of(110L), null);
		sysRoleScopeService.replace(6L, List.of(form));

		ArgumentCaptor<List<RoleScopeEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(sysRoleScopeService).saveBatch(captor.capture(), eq(BatchSizes.SIZE_500));
		assertThat(captor.getValue().get(0).getScopeType()).isEqualTo("SELF");
		assertThat(captor.getValue().get(0).getScopeIds()).isNull();
		verify(sysDeptMapper, never()).selectByIds(any());
	}

	@Test
	@DisplayName("replace：空列表删除已有配置并触发一次失效")
	void replaceEmptyWhenExistsShouldRemoveAndInvalidate() {
		when(rbacReferenceChecker.getExisting(3L)).thenReturn(role(3L, "REGION_MGR_NORTH"));
		when(roleScopeMapper.delete(any(Wrapper.class))).thenReturn(1);

		sysRoleScopeService.replace(3L, List.of());

		verify(sysRoleScopeService, never()).saveBatch(anyList(), anyInt());
		verify(roleScopeMapper).delete(any(Wrapper.class));
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("REGION_MGR_NORTH"), "replace-role-scope");
	}

	@Test
	@DisplayName("replace：空列表且无配置行时不触发失效")
	void replaceEmptyWhenMissingShouldBeIdempotent() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));
		when(roleScopeMapper.delete(any(Wrapper.class))).thenReturn(0);

		sysRoleScopeService.replace(1L, List.of());

		verify(roleScopeMapper).delete(any(Wrapper.class));
		verify(sysRoleScopeService, never()).saveBatch(anyList(), anyInt());
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
	}

}
