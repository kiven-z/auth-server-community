package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.admin.user.UserScopeMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.UserScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.user.SysUserScopeVO;
import com.auth.service.system.admin.support.scope.DataScopeFormSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
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
 * {@link SysUserScopeServiceImpl} 单元测试
 */
@DisplayName("SysUserScopeServiceImpl 用户数据范围")
@ExtendWith(MockitoExtension.class)
class SysUserScopeServiceImplTest {

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private UserAuthorizationInvalidationTrigger userInvalidationTrigger;

	@Mock
	private UserScopeMapper userScopeMapper;

	private SysUserScopeServiceImpl sysUserScopeService;

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
		sysUserScopeService = spy(
				new SysUserScopeServiceImpl(userReferenceChecker, dataScopeFormSupport, userInvalidationTrigger));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserScopeService, userScopeMapper);
	}

	@Test
	@DisplayName("用户不存在时抛出 USER_NOT_FOUND")
	void getByUserIdWhenUserMissingShouldThrow() {
		when(userReferenceChecker.getExistingActive(99L))
			.thenThrow(new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND));

		assertThatThrownBy(() -> sysUserScopeService.getByUserId(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("无 user_scope 行时返回 null（继承角色）")
	void getByUserIdWhenNoScopeRowShouldReturnNull() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

		assertThat(sysUserScopeService.getByUserId(1L)).isNull();
	}

	@Test
	@DisplayName("有配置行时回显类型与部门 ID 列表")
	void getByUserIdShouldReturnScopeAndDeptIds() {
		when(userReferenceChecker.getExistingActive(3L)).thenReturn(new UserEntity());

		UserScopeEntity entity = new UserScopeEntity();
		entity.setId(100L);
		entity.setUserId(3L);
		entity.setScopeType("DEPT_AND_CHILD");
		entity.setScopeDeptIds("[110,120]");
		entity.setRemark("华北区覆盖");
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(entity);

		SysUserScopeVO vo = sysUserScopeService.getByUserId(3L);

		assertThat(vo).isNotNull();
		assertThat(vo.getId()).isEqualTo(100L);
		assertThat(vo.getUserId()).isEqualTo(3L);
		assertThat(vo.getScopeType()).isEqualTo("DEPT_AND_CHILD");
		assertThat(vo.getScopeDeptIds()).containsExactly(110L, 120L);
		assertThat(vo.getRemark()).isEqualTo("华北区覆盖");
	}

	@Test
	@DisplayName("scope_dept_ids 为空时返回空部门列表")
	void getByUserIdWhenDeptIdsBlankShouldReturnEmptyList() {
		when(userReferenceChecker.getExistingActive(2L)).thenReturn(new UserEntity());

		UserScopeEntity entity = new UserScopeEntity();
		entity.setId(200L);
		entity.setUserId(2L);
		entity.setScopeType("SELF");
		entity.setScopeDeptIds(null);
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(entity);

		SysUserScopeVO vo = sysUserScopeService.getByUserId(2L);

		assertThat(vo.getScopeType()).isEqualTo("SELF");
		assertThat(vo.getScopeDeptIds()).isEmpty();
	}

	@Test
	@DisplayName("upsert：无行时插入并触发失效")
	void upsertWhenMissingShouldInsertAndInvalidate() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);
		doReturn(true).when(sysUserScopeService).saveOrUpdate(any(UserScopeEntity.class));

		SysDataScopeForm form = form("ALL", null, "全量覆盖");
		sysUserScopeService.upsert(1L, form);

		ArgumentCaptor<UserScopeEntity> captor = ArgumentCaptor.forClass(UserScopeEntity.class);
		verify(sysUserScopeService).saveOrUpdate(captor.capture());
		assertThat(captor.getValue().getId()).isNull();
		assertThat(captor.getValue().getUserId()).isEqualTo(1L);
		assertThat(captor.getValue().getScopeType()).isEqualTo("ALL");
		assertThat(captor.getValue().getScopeDeptIds()).isNull();
		assertThat(captor.getValue().getRemark()).isEqualTo("全量覆盖");
		verify(userInvalidationTrigger).submitByUserIds(List.of(1L), "upsert-user-scope");
	}

	@Test
	@DisplayName("upsert：已有行时更新并触发失效")
	void upsertWhenExistsShouldUpdateAndInvalidate() {
		when(userReferenceChecker.getExistingActive(3L)).thenReturn(new UserEntity());

		UserScopeEntity existing = new UserScopeEntity();
		existing.setId(100L);
		existing.setUserId(3L);
		existing.setScopeType("SELF");
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(existing);
		doReturn(true).when(sysUserScopeService).saveOrUpdate(any(UserScopeEntity.class));

		SysDataScopeForm form = form("DEPT_AND_CHILD", List.of(110L, 110L, 120L), "华北");
		when(sysDeptMapper.selectByIds(List.of(110L, 120L))).thenReturn(List.of(dept(110L), dept(120L)));
		sysUserScopeService.upsert(3L, form);

		verify(sysDeptMapper).selectByIds(List.of(110L, 120L));
		ArgumentCaptor<UserScopeEntity> captor = ArgumentCaptor.forClass(UserScopeEntity.class);
		verify(sysUserScopeService).saveOrUpdate(captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(100L);
		assertThat(captor.getValue().getScopeType()).isEqualTo("DEPT_AND_CHILD");
		assertThat(captor.getValue().getScopeDeptIds()).isEqualTo("[110,120]");
		verify(userInvalidationTrigger).submitByUserIds(List.of(3L), "upsert-user-scope");
	}

	@Test
	@DisplayName("upsert：非法 scopeType 抛出 DATA_INVALID")
	void upsertWhenScopeTypeInvalidShouldThrow() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());

		SysDataScopeForm form = form("FROM_PROFILE", null, null);
		assertThatThrownBy(() -> sysUserScopeService.upsert(1L, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
		verify(userInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("upsert：DEPT 未传部门 ID 抛出 PARAM_REQUIRED")
	void upsertWhenDeptScopeMissingDeptIdsShouldThrow() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());

		SysDataScopeForm form = form("DEPT", List.of(), null);
		assertThatThrownBy(() -> sysUserScopeService.upsert(1L, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(sysDeptMapper, never()).selectByIds(any());
	}

	@Test
	@DisplayName("upsert：部门 ID 无效时抛出 GRANT_REFERENCE_INVALID")
	void upsertWhenDeptInvalidShouldThrow() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(sysDeptMapper.selectByIds(List.of(99L))).thenReturn(List.of());

		SysDataScopeForm form = form("DEPT", List.of(99L), null);
		assertThatThrownBy(() -> sysUserScopeService.upsert(1L, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(userInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("upsert：SELF 忽略表单中的部门 ID")
	void upsertSelfShouldClearDeptIds() {
		when(userReferenceChecker.getExistingActive(6L)).thenReturn(new UserEntity());
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);
		doReturn(true).when(sysUserScopeService).saveOrUpdate(any(UserScopeEntity.class));

		SysDataScopeForm form = form("SELF", List.of(110L), null);
		sysUserScopeService.upsert(6L, form);

		ArgumentCaptor<UserScopeEntity> captor = ArgumentCaptor.forClass(UserScopeEntity.class);
		verify(sysUserScopeService).saveOrUpdate(captor.capture());
		assertThat(captor.getValue().getScopeType()).isEqualTo("SELF");
		assertThat(captor.getValue().getScopeDeptIds()).isNull();
		verify(sysDeptMapper, never()).selectByIds(any());
	}

	@Test
	@DisplayName("clear：有配置行时删除并触发失效")
	void clearByUserIdWhenExistsShouldRemoveAndInvalidate() {
		when(userReferenceChecker.getExistingActive(3L)).thenReturn(new UserEntity());

		UserScopeEntity existing = new UserScopeEntity();
		existing.setId(100L);
		existing.setUserId(3L);
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(existing);
		doReturn(true).when(sysUserScopeService).removeById(100L);

		sysUserScopeService.clearByUserId(3L);

		verify(sysUserScopeService).removeById(100L);
		verify(userInvalidationTrigger).submitByUserIds(List.of(3L), "clear-user-scope");
	}

	@Test
	@DisplayName("clear：无配置行时幂等且不触发失效")
	void clearByUserIdWhenMissingShouldBeIdempotent() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(userScopeMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

		sysUserScopeService.clearByUserId(1L);

		verify(sysUserScopeService, never()).removeById(any());
		verify(userInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

}
