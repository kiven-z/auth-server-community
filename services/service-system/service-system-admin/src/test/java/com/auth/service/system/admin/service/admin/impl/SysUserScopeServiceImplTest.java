package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.constants.BatchSizes;
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
	@DisplayName("无 user_scope 行时返回空列表（继承角色）")
	void getByUserIdWhenNoScopeRowShouldReturnEmpty() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(userScopeMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

		assertThat(sysUserScopeService.getByUserId(1L)).isEmpty();
	}

	@Test
	@DisplayName("有配置行时回显维度、类型与节点 ID 列表")
	void getByUserIdShouldReturnScopeAndIds() {
		when(userReferenceChecker.getExistingActive(3L)).thenReturn(new UserEntity());

		UserScopeEntity entity = new UserScopeEntity();
		entity.setId(100L);
		entity.setUserId(3L);
		entity.setDimension("DEPT");
		entity.setScopeType("IDS_AND_CHILD");
		entity.setScopeIds("[110,120]");
		entity.setRemark("华北区覆盖");
		when(userScopeMapper.selectList(any(Wrapper.class))).thenReturn(List.of(entity));

		List<SysUserScopeVO> scopes = sysUserScopeService.getByUserId(3L);

		assertThat(scopes).hasSize(1);
		SysUserScopeVO vo = scopes.get(0);
		assertThat(vo.getId()).isEqualTo(100L);
		assertThat(vo.getUserId()).isEqualTo(3L);
		assertThat(vo.getDimension()).isEqualTo("DEPT");
		assertThat(vo.getScopeType()).isEqualTo("IDS_AND_CHILD");
		assertThat(vo.getScopeIds()).containsExactly(110L, 120L);
		assertThat(vo.getRemark()).isEqualTo("华北区覆盖");
	}

	@Test
	@DisplayName("scope_ids 为空时返回空节点列表")
	void getByUserIdWhenIdsBlankShouldReturnEmptyList() {
		when(userReferenceChecker.getExistingActive(2L)).thenReturn(new UserEntity());

		UserScopeEntity entity = new UserScopeEntity();
		entity.setId(200L);
		entity.setUserId(2L);
		entity.setDimension("DEPT");
		entity.setScopeType("SELF");
		entity.setScopeIds(null);
		when(userScopeMapper.selectList(any(Wrapper.class))).thenReturn(List.of(entity));

		List<SysUserScopeVO> scopes = sysUserScopeService.getByUserId(2L);

		assertThat(scopes).hasSize(1);
		assertThat(scopes.get(0).getScopeType()).isEqualTo("SELF");
		assertThat(scopes.get(0).getScopeIds()).isEmpty();
	}

	@Test
	@DisplayName("replace：无行时删除零行后插入并触发一次失效")
	void replaceWhenMissingShouldInsertAndInvalidate() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(userScopeMapper.delete(any(Wrapper.class))).thenReturn(0);
		doReturn(true).when(sysUserScopeService).saveBatch(anyList(), anyInt());

		SysDataScopeForm form = form("DEPT", "ALL", null, "全量覆盖");
		sysUserScopeService.replace(1L, List.of(form));

		ArgumentCaptor<List<UserScopeEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(userScopeMapper).delete(any(Wrapper.class));
		verify(sysUserScopeService).saveBatch(captor.capture(), eq(BatchSizes.SIZE_500));
		assertThat(captor.getValue()).hasSize(1);
		UserScopeEntity entity = captor.getValue().get(0);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getUserId()).isEqualTo(1L);
		assertThat(entity.getDimension()).isEqualTo("DEPT");
		assertThat(entity.getScopeType()).isEqualTo("ALL");
		assertThat(entity.getScopeIds()).isNull();
		assertThat(entity.getRemark()).isEqualTo("全量覆盖");
		verify(userInvalidationTrigger).submitByUserIds(List.of(1L), "replace-user-scope");
	}

	@Test
	@DisplayName("replace：已有行时先删除再插入并触发一次失效")
	void replaceWhenExistsShouldDeleteThenInsert() {
		when(userReferenceChecker.getExistingActive(3L)).thenReturn(new UserEntity());
		when(userScopeMapper.delete(any(Wrapper.class))).thenReturn(1);
		doReturn(true).when(sysUserScopeService).saveBatch(anyList(), anyInt());

		SysDataScopeForm form = form("DEPT", "IDS_AND_CHILD", List.of(110L, 110L, 120L), "华北");
		when(sysDeptMapper.selectByIds(List.of(110L, 120L))).thenReturn(List.of(dept(110L), dept(120L)));
		sysUserScopeService.replace(3L, List.of(form));

		verify(sysDeptMapper).selectByIds(List.of(110L, 120L));
		ArgumentCaptor<List<UserScopeEntity>> captor = ArgumentCaptor.forClass(List.class);
		InOrder inOrder = inOrder(userScopeMapper, sysUserScopeService);
		inOrder.verify(userScopeMapper).delete(any(Wrapper.class));
		inOrder.verify(sysUserScopeService).saveBatch(captor.capture(), eq(BatchSizes.SIZE_500));
		UserScopeEntity entity = captor.getValue().get(0);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getUserId()).isEqualTo(3L);
		assertThat(entity.getDimension()).isEqualTo("DEPT");
		assertThat(entity.getScopeType()).isEqualTo("IDS_AND_CHILD");
		assertThat(entity.getScopeIds()).isEqualTo("[110,120]");
		assertThat(entity.getRemark()).isEqualTo("华北");
		verify(userInvalidationTrigger).submitByUserIds(List.of(3L), "replace-user-scope");
	}

	@Test
	@DisplayName("replace：同一维重复时抛出 PARAM_DUPLICATE")
	void replaceWhenDimensionDuplicatedShouldThrow() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());

		assertThatThrownBy(() -> sysUserScopeService.replace(1L,
				List.of(form("DEPT", "ALL", null, null), form("DEPT", "SELF", null, null))))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);
		verify(userScopeMapper, never()).delete(any());
		verify(sysUserScopeService, never()).saveBatch(anyList(), anyInt());
		verify(userInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("replace：非法 scopeType 抛出 DATA_INVALID")
	void replaceWhenScopeTypeInvalidShouldThrow() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());

		SysDataScopeForm form = form("DEPT", "UNKNOWN", null, null);
		assertThatThrownBy(() -> sysUserScopeService.replace(1L, List.of(form)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_INVALID);
		verify(userScopeMapper, never()).delete(any());
		verify(sysUserScopeService, never()).saveBatch(anyList(), anyInt());
		verify(userInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("replace：IDS 未传节点 ID 抛出 PARAM_REQUIRED")
	void replaceWhenIdsScopeMissingIdsShouldThrow() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());

		SysDataScopeForm form = form("DEPT", "IDS", List.of(), null);
		assertThatThrownBy(() -> sysUserScopeService.replace(1L, List.of(form)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
		verify(sysDeptMapper, never()).selectByIds(any());
		verify(userScopeMapper, never()).delete(any());
		verify(sysUserScopeService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("replace：部门 ID 无效时抛出 GRANT_REFERENCE_INVALID")
	void replaceWhenDeptInvalidShouldThrow() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(sysDeptMapper.selectByIds(List.of(99L))).thenReturn(List.of());

		SysDataScopeForm form = form("DEPT", "IDS", List.of(99L), null);
		assertThatThrownBy(() -> sysUserScopeService.replace(1L, List.of(form)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(userScopeMapper, never()).delete(any());
		verify(sysUserScopeService, never()).saveBatch(anyList(), anyInt());
		verify(userInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("replace：SELF 忽略表单中的节点 ID")
	void replaceSelfShouldClearIds() {
		when(userReferenceChecker.getExistingActive(6L)).thenReturn(new UserEntity());
		when(userScopeMapper.delete(any(Wrapper.class))).thenReturn(0);
		doReturn(true).when(sysUserScopeService).saveBatch(anyList(), anyInt());

		SysDataScopeForm form = form("DEPT", "SELF", List.of(110L), null);
		sysUserScopeService.replace(6L, List.of(form));

		ArgumentCaptor<List<UserScopeEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(sysUserScopeService).saveBatch(captor.capture(), eq(BatchSizes.SIZE_500));
		assertThat(captor.getValue().get(0).getScopeType()).isEqualTo("SELF");
		assertThat(captor.getValue().get(0).getScopeIds()).isNull();
		verify(sysDeptMapper, never()).selectByIds(any());
	}

	@Test
	@DisplayName("replace：空列表删除已有配置并触发一次失效")
	void replaceEmptyWhenExistsShouldRemoveAndInvalidate() {
		when(userReferenceChecker.getExistingActive(3L)).thenReturn(new UserEntity());
		when(userScopeMapper.delete(any(Wrapper.class))).thenReturn(1);

		sysUserScopeService.replace(3L, List.of());

		verify(sysUserScopeService, never()).saveBatch(anyList(), anyInt());
		verify(userScopeMapper).delete(any(Wrapper.class));
		verify(userInvalidationTrigger).submitByUserIds(List.of(3L), "replace-user-scope");
	}

	@Test
	@DisplayName("replace：空列表且无配置行时不触发失效")
	void replaceEmptyWhenMissingShouldBeIdempotent() {
		when(userReferenceChecker.getExistingActive(1L)).thenReturn(new UserEntity());
		when(userScopeMapper.delete(any(Wrapper.class))).thenReturn(0);

		sysUserScopeService.replace(1L, List.of());

		verify(userScopeMapper).delete(any(Wrapper.class));
		verify(sysUserScopeService, never()).saveBatch(anyList(), anyInt());
		verify(userInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

}
