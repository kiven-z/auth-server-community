package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.mapper.admin.user.UserDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.form.user.UserDeptAssignForm;
import com.auth.service.system.admin.model.po.user.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.user.UserDeptPageVO;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOrgRelationBatchRemoveSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserDeptServiceImpl} 单元测试
 */
@DisplayName("SysUserDeptServiceImpl 用户部门关联")
@ExtendWith(MockitoExtension.class)
class SysUserDeptServiceImplTest {

	private static final Long USER_ID = 10L;

	private static final Long RELATION_ID = 100L;

	private static final Long DEPT_ID = 20L;

	private static final Long OTHER_DEPT_ID = 21L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private UserDeptMapper userDeptMapper;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Captor
	private ArgumentCaptor<UserDeptEntity> entityCaptor;

	private SysUserDeptServiceImpl sysUserDeptService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SysUserDeptServiceImplTest.USER_ID);
		entity.setUsername("tester");
		return entity;
	}

	private static SysDeptEntity activeDept() {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(DEPT_ID);
		entity.setDeptName("研发部");
		entity.setDeptCode("RD");
		entity.setStatus(true);
		return entity;
	}

	private static UserDeptAssignForm createForm(Boolean isPrimary, String remark) {
		UserDeptAssignForm form = new UserDeptAssignForm();
		form.setDeptId(DEPT_ID);
		form.setIsPrimary(isPrimary);
		form.setRemark(remark);
		return form;
	}

	private static UserDeptEntity existingRelation() {
		UserDeptEntity entity = new UserDeptEntity();
		entity.setId(RELATION_ID);
		entity.setUserId(USER_ID);
		entity.setDeptId(DEPT_ID);
		entity.setIsPrimary(false);
		entity.setRemark("原备注");
		entity.setVersion(1L);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		DeptReferenceChecker deptReferenceChecker = new DeptReferenceChecker(sysDeptMapper);
		UserOrgRelationBatchRemoveSupport batchRemoveSupport = new UserOrgRelationBatchRemoveSupport(
				userReferenceChecker, userAuthorizationInvalidationTrigger);
		sysUserDeptService = new SysUserDeptServiceImpl(userReferenceChecker, deptReferenceChecker,
				userAuthorizationInvalidationTrigger, auditUserDisplayService, batchRemoveSupport);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserDeptService, userDeptMapper);
	}

	@Test
	@DisplayName("分页查询：映射业务字段并补充审计用户名")
	void getPageMapsRowsAndEnrichesAuditUsernames() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserDeptPageQuery query = new UserDeptPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		Instant now = LocalDateTime.of(2026, 6, 11, 10, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		UserDeptPageRowPO row = new UserDeptPageRowPO();
		row.setId(RELATION_ID);
		row.setUserId(USER_ID);
		row.setDeptId(DEPT_ID);
		row.setDeptName("研发部");
		row.setDeptCode("RD");
		row.setIsPrimary(true);
		row.setRemark("负责人");
		row.setCreatedAt(now);
		row.setUpdatedAt(now);
		row.setCreatedBy(1L);
		row.setUpdatedBy(1L);
		Page<UserDeptPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(row));
		poPage.setTotal(1);
		when(userDeptMapper.selectListByPage(any(Page.class), eq(USER_ID), eq(query))).thenReturn(poPage);

		PageResponse<UserDeptPageVO> result = sysUserDeptService.getPage(USER_ID, query);

		assertThat(result.getList()).hasSize(1);
		UserDeptPageVO vo = result.getList().get(0);
		assertThat(vo.getId()).isEqualTo(RELATION_ID);
		assertThat(vo.getUserId()).isEqualTo(USER_ID);
		assertThat(vo.getDeptId()).isEqualTo(DEPT_ID);
		assertThat(vo.getDeptName()).isEqualTo("研发部");
		assertThat(vo.getDeptCode()).isEqualTo("RD");
		assertThat(vo.getIsPrimary()).isTrue();
		assertThat(vo.getRemark()).isEqualTo("负责人");
		assertThat(vo.getCreatedAt()).isEqualTo(now);
		assertThat(vo.getUpdatedAt()).isEqualTo(now);
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("创建关联：非主部门时不降级其他关联")
	void createNonPrimarySkipsDemote() {
		stubCreatePrerequisites();
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(0);
		when(userDeptMapper.insert(any(UserDeptEntity.class))).thenReturn(1);

		sysUserDeptService.create(USER_ID, createForm(false, "备注"));

		verify(userDeptMapper, never()).demotePrimaryByUserId(USER_ID);
		verify(userDeptMapper).insert(entityCaptor.capture());
		UserDeptEntity saved = entityCaptor.getValue();
		assertThat(saved.getUserId()).isEqualTo(USER_ID);
		assertThat(saved.getDeptId()).isEqualTo(DEPT_ID);
		assertThat(saved.getIsPrimary()).isFalse();
		assertThat(saved.getRemark()).isEqualTo("备注");
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "create-dept");
	}

	@Test
	@DisplayName("创建关联：主部门时降级该用户其他主部门")
	void createPrimaryDemotesExistingPrimary() {
		stubCreatePrerequisites();
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(0);
		when(userDeptMapper.insert(any(UserDeptEntity.class))).thenReturn(1);

		sysUserDeptService.create(USER_ID, createForm(true, null));

		verify(userDeptMapper).demotePrimaryByUserId(USER_ID);
		verify(userDeptMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getIsPrimary()).isTrue();
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "create-dept");
	}

	@Test
	@DisplayName("创建关联：重复部门时拒绝保存")
	void createRejectsDuplicateDeptRelation() {
		stubCreatePrerequisites();
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(1);
		UserDeptAssignForm form = createForm(false, null);

		assertThatThrownBy(() -> sysUserDeptService.create(USER_ID, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_DUPLICATE);

		verify(userDeptMapper, never()).insert(any(UserDeptEntity.class));
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("创建关联：停用或传播禁用部门不可新增任职")
	void createRejectsDisabledDept() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(activeDept());
		when(sysDeptMapper.countEffectiveById(DEPT_ID)).thenReturn(0L);
		UserDeptAssignForm form = createForm(false, null);

		assertThatThrownBy(() -> sysUserDeptService.create(USER_ID, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);

		verify(userDeptMapper, never()).insert(any(UserDeptEntity.class));
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	private void stubCreatePrerequisites() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(activeDept());
		when(sysDeptMapper.countEffectiveById(DEPT_ID)).thenReturn(1L);
	}

	@Test
	@DisplayName("更新关联：仅修改备注时不降级主部门并触发授权失效")
	void updateRemarkOnlySkipsDemoteAndTriggersInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserDeptEntity existing = existingRelation();
		when(userDeptMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existing);
		when(userDeptMapper.updateById(any(UserDeptEntity.class))).thenReturn(1);

		sysUserDeptService.update(USER_ID, RELATION_ID, createForm(false, "新备注"));

		verify(userDeptMapper, never()).demotePrimaryByUserId(USER_ID);
		verify(userDeptMapper).updateById(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getRemark()).isEqualTo("新备注");
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "update-dept");
	}

	@Test
	@DisplayName("更新关联：设为主部门时降级该用户其他主部门")
	void updatePrimaryDemotesExistingPrimary() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existingRelation());
		when(userDeptMapper.updateById(any(UserDeptEntity.class))).thenReturn(1);

		sysUserDeptService.update(USER_ID, RELATION_ID, createForm(true, null));

		verify(userDeptMapper).demotePrimaryByUserId(USER_ID);
		verify(userDeptMapper).updateById(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getIsPrimary()).isTrue();
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "update-dept");
	}

	@Test
	@DisplayName("更新关联：部门改到已存在关联时拒绝保存")
	void updateRejectsDuplicateDeptRelation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existingRelation());
		SysDeptEntity otherDept = activeDept();
		otherDept.setId(OTHER_DEPT_ID);
		when(sysDeptMapper.selectById(OTHER_DEPT_ID)).thenReturn(otherDept);
		when(sysDeptMapper.countEffectiveById(OTHER_DEPT_ID)).thenReturn(1L);
		UserDeptEntity conflict = new UserDeptEntity();
		conflict.setId(200L);
		conflict.setUserId(USER_ID);
		conflict.setDeptId(OTHER_DEPT_ID);
		when(userDeptMapper.selectByUserIdAndDeptId(USER_ID, OTHER_DEPT_ID)).thenReturn(conflict);
		UserDeptAssignForm form = createForm(false, null);
		form.setDeptId(OTHER_DEPT_ID);

		assertThatThrownBy(() -> sysUserDeptService.update(USER_ID, RELATION_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_DUPLICATE);

		verify(userDeptMapper, never()).updateById(any(UserDeptEntity.class));
	}

	@Test
	@DisplayName("更新关联：改到停用或传播禁用部门时拒绝")
	void updateRejectsDisabledDept() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existingRelation());
		SysDeptEntity otherDept = activeDept();
		otherDept.setId(OTHER_DEPT_ID);
		when(sysDeptMapper.selectById(OTHER_DEPT_ID)).thenReturn(otherDept);
		when(sysDeptMapper.countEffectiveById(OTHER_DEPT_ID)).thenReturn(0L);
		UserDeptAssignForm form = createForm(false, null);
		form.setDeptId(OTHER_DEPT_ID);

		assertThatThrownBy(() -> sysUserDeptService.update(USER_ID, RELATION_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);

		verify(userDeptMapper, never()).updateById(any(UserDeptEntity.class));
	}

	@Test
	@DisplayName("更新关联：记录不存在时拒绝保存")
	void updateRejectsMissingRelation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(null);
		UserDeptAssignForm form = createForm(false, null);

		assertThatThrownBy(() -> sysUserDeptService.update(USER_ID, RELATION_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verify(userDeptMapper, never()).updateById(any(UserDeptEntity.class));
	}

	@Test
	@DisplayName("批量删除：删除并触发授权失效")
	void removeBatchDeletesOwnedRelationsAndTriggersInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.deleteByIds(anyCollection())).thenReturn(1);

		sysUserDeptService.removeBatch(USER_ID, List.of(RELATION_ID));

		verify(userDeptMapper).deleteByIds(anyCollection());
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "delete-dept");
	}

	@Test
	@DisplayName("批量删除：空列表时不执行删除")
	void removeBatchSkipsEmptyIds() {
		sysUserDeptService.removeBatch(USER_ID, List.of());

		verify(userDeptMapper, never()).deleteByIds(anyCollection());
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("全部清空：删除用户全部关联并触发授权失效")
	void removeAllDeletesAllRelationsAndTriggersInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.deleteByUserId(USER_ID)).thenReturn(2);

		sysUserDeptService.removeAll(USER_ID);

		verify(userDeptMapper).deleteByUserId(USER_ID);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "clear-dept");
	}

	@Test
	@DisplayName("全部清空：无关联时不触发授权失效")
	void removeAllSkipsInvalidationWhenNoRelations() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.deleteByUserId(USER_ID)).thenReturn(0);

		sysUserDeptService.removeAll(USER_ID);

		verify(userDeptMapper).deleteByUserId(USER_ID);
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

}
