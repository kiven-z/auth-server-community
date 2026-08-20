package com.auth.service.system.admin.service.admin.impl;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

	private static SysDataScopeForm form(String scopeType, List<Long> deptIds, String remark) {
		SysDataScopeForm form = new SysDataScopeForm();
		form.setScopeType(scopeType);
		form.setScopeDeptIds(deptIds);
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
	@DisplayName("无 role_scope 行时返回 null")
	void getByRoleIdWhenNoScopeRowShouldReturnNull() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role);
		when(roleScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

		assertThat(sysRoleScopeService.getByRoleId(1L)).isNull();
	}

	@Test
	@DisplayName("有配置行时回显类型与部门 ID 列表")
	void getByRoleIdShouldReturnScopeAndDeptIds() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(3L);
		when(rbacReferenceChecker.getExisting(3L)).thenReturn(role);

		RoleScopeEntity entity = new RoleScopeEntity();
		entity.setId(100L);
		entity.setRoleId(3L);
		entity.setScopeType("DEPT_AND_CHILD");
		entity.setScopeDeptIds("[110,120]");
		entity.setRemark("华北区");
		when(roleScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(entity);

		SysRoleScopeVO vo = sysRoleScopeService.getByRoleId(3L);

		assertThat(vo).isNotNull();
		assertThat(vo.getId()).isEqualTo(100L);
		assertThat(vo.getRoleId()).isEqualTo(3L);
		assertThat(vo.getScopeType()).isEqualTo("DEPT_AND_CHILD");
		assertThat(vo.getScopeDeptIds()).containsExactly(110L, 120L);
		assertThat(vo.getRemark()).isEqualTo("华北区");
	}

	@Test
	@DisplayName("scope_dept_ids 为空时返回空部门列表")
	void getByRoleIdWhenDeptIdsBlankShouldReturnEmptyList() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(2L);
		when(rbacReferenceChecker.getExisting(2L)).thenReturn(role);

		RoleScopeEntity entity = new RoleScopeEntity();
		entity.setId(200L);
		entity.setRoleId(2L);
		entity.setScopeType("ALL");
		entity.setScopeDeptIds(null);
		when(roleScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(entity);

		SysRoleScopeVO vo = sysRoleScopeService.getByRoleId(2L);

		assertThat(vo.getScopeType()).isEqualTo("ALL");
		assertThat(vo.getScopeDeptIds()).isEmpty();
	}

	@Test
	@DisplayName("upsert：无行时插入并触发失效")
	void upsertWhenMissingShouldInsertAndInvalidate() {
		SysRoleEntity role = role(1L, "ADMIN");
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role);
		when(roleScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);
		doReturn(true).when(sysRoleScopeService).saveOrUpdate(any(RoleScopeEntity.class));

		SysDataScopeForm form = form("ALL", null, "全量");
		sysRoleScopeService.upsert(1L, form);

		ArgumentCaptor<RoleScopeEntity> captor = ArgumentCaptor.forClass(RoleScopeEntity.class);
		verify(sysRoleScopeService).saveOrUpdate(captor.capture());
		assertThat(captor.getValue().getId()).isNull();
		assertThat(captor.getValue().getRoleId()).isEqualTo(1L);
		assertThat(captor.getValue().getScopeType()).isEqualTo("ALL");
		assertThat(captor.getValue().getScopeDeptIds()).isNull();
		assertThat(captor.getValue().getRemark()).isEqualTo("全量");
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "upsert-role-scope");
	}

	@Test
	@DisplayName("upsert：已有行时更新并触发失效")
	void upsertWhenExistsShouldUpdateAndInvalidate() {
		SysRoleEntity role = role(3L, "REGION_MGR_NORTH");
		when(rbacReferenceChecker.getExisting(3L)).thenReturn(role);

		RoleScopeEntity existing = new RoleScopeEntity();
		existing.setId(100L);
		existing.setRoleId(3L);
		existing.setScopeType("SELF");
		when(roleScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(existing);
		doReturn(true).when(sysRoleScopeService).saveOrUpdate(any(RoleScopeEntity.class));

		SysDataScopeForm form = form("DEPT_AND_CHILD", List.of(110L, 110L, 120L), "华北");
		when(sysDeptMapper.selectByIds(List.of(110L, 120L))).thenReturn(List.of(dept(110L), dept(120L)));
		sysRoleScopeService.upsert(3L, form);

		verify(sysDeptMapper).selectByIds(List.of(110L, 120L));
		ArgumentCaptor<RoleScopeEntity> captor = ArgumentCaptor.forClass(RoleScopeEntity.class);
		verify(sysRoleScopeService).saveOrUpdate(captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(100L);
		assertThat(captor.getValue().getScopeType()).isEqualTo("DEPT_AND_CHILD");
		assertThat(captor.getValue().getScopeDeptIds()).isEqualTo("[110,120]");
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("REGION_MGR_NORTH"), "upsert-role-scope");
	}

	@Test
	@DisplayName("upsert：非法 scopeType 抛出 DATA_INVALID")
	void upsertWhenScopeTypeInvalidShouldThrow() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));

		SysDataScopeForm form = form("FROM_PROFILE", null, null);
		assertThatThrownBy(() -> sysRoleScopeService.upsert(1L, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
	}

	@Test
	@DisplayName("upsert：DEPT 未传部门 ID 抛出 PARAM_REQUIRED")
	void upsertWhenDeptScopeMissingDeptIdsShouldThrow() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));

		SysDataScopeForm form = form("DEPT", List.of(), null);
		assertThatThrownBy(() -> sysRoleScopeService.upsert(1L, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(sysDeptMapper, never()).selectByIds(any());
	}

	@Test
	@DisplayName("upsert：部门 ID 无效时抛出 GRANT_REFERENCE_INVALID")
	void upsertWhenDeptInvalidShouldThrow() {
		when(rbacReferenceChecker.getExisting(1L)).thenReturn(role(1L, "ADMIN"));
		when(sysDeptMapper.selectByIds(List.of(99L))).thenReturn(List.of());

		SysDataScopeForm form = form("DEPT", List.of(99L), null);
		assertThatThrownBy(() -> sysRoleScopeService.upsert(1L, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
	}

	@Test
	@DisplayName("upsert：SELF 忽略表单中的部门 ID")
	void upsertSelfShouldClearDeptIds() {
		SysRoleEntity role = role(6L, "DEPT_MGR");
		when(rbacReferenceChecker.getExisting(6L)).thenReturn(role);
		when(roleScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);
		doReturn(true).when(sysRoleScopeService).saveOrUpdate(any(RoleScopeEntity.class));

		SysDataScopeForm form = form("SELF", List.of(110L), null);
		sysRoleScopeService.upsert(6L, form);

		ArgumentCaptor<RoleScopeEntity> captor = ArgumentCaptor.forClass(RoleScopeEntity.class);
		verify(sysRoleScopeService).saveOrUpdate(captor.capture());
		assertThat(captor.getValue().getScopeType()).isEqualTo("SELF");
		assertThat(captor.getValue().getScopeDeptIds()).isNull();
		verify(sysDeptMapper, never()).selectByIds(any());
	}

}
