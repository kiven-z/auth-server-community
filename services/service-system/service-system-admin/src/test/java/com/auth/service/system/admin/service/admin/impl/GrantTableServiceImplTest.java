package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.support.grant.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.grant.TypedGrantTableActiveSubjectChecker;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link GrantTableServiceImpl} 单元测试
 */
@DisplayName("GrantTableServiceImpl grant_table 读写与授权编排")
@ExtendWith(MockitoExtension.class)
class GrantTableServiceImplTest {

	private static final Long SUBJECT_ID = 10L;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private RbacReferenceChecker rbacReferenceChecker;

	@Mock
	private GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	private GrantTableServiceImpl grantTableService;

	private static SysPostEntity activePost() {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(SUBJECT_ID);
		entity.setDeptId(1L);
		entity.setPostCode("DEV");
		entity.setPostName("开发岗");
		entity.setStatus(true);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper);
		TypedGrantTableActiveSubjectChecker postSubjectChecker = new TypedGrantTableActiveSubjectChecker(
				GrantTableSubjectType.POST, postReferenceChecker::requireEffective);
		GrantTableSubjectExistenceVerifier subjectExistenceVerifier = new GrantTableSubjectExistenceVerifier(
				List.of(postSubjectChecker));
		grantTableService = spy(
				new GrantTableServiceImpl(subjectExistenceVerifier, rbacReferenceChecker, grantInvalidationTrigger));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(grantTableService, grantTableMapper);
		lenient().doReturn(true).when(grantTableService).saveBatch(any(Collection.class));
	}

	@Test
	@DisplayName("查询已分配角色：主体存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenSubjectExists() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		when(sysPostMapper.countEffectiveById(SUBJECT_ID)).thenReturn(1L);
		RoleReferencePO boundRole = new RoleReferencePO();
		boundRole.setId(101L);
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");
		boundRole.setStatus(true);
		when(grantTableMapper.selectAssignedRolesBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID))
			.thenReturn(List.of(boundRole));

		List<RoleReferenceVO> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.POST, SUBJECT_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getId()).isEqualTo(101L);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务角色A");
	}

	@Test
	@DisplayName("查询已分配角色：主体不存在时抛业务异常")
	void listAssignedRolesThrowsWhenSubjectMissing() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(null);

		assertThatThrownBy(() -> grantTableService.listAssignedRoles(GrantTableSubjectType.POST, SUBJECT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("组织类全量覆盖：写入 grant 并提交失效")
	void replaceOrgSubjectRolesWritesGrantAndSubmitsInvalidation() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		when(sysPostMapper.countEffectiveById(SUBJECT_ID)).thenReturn(1L);
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleIds(List.of(101L, 102L));

		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(rbacReferenceChecker).requireExistingEnabledRoleIds(List.of(101L, 102L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.POST.name(), List.of(SUBJECT_ID));
		verify(grantTableService).saveBatch(any(Collection.class));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<GrantSubjectKey>> subjectsCaptor = ArgumentCaptor.forClass(List.class);
		verify(grantInvalidationTrigger).submitByGrantSubjects(subjectsCaptor.capture(), eq("replace-roles"));
		assertThat(subjectsCaptor.getValue())
			.containsExactly(new GrantSubjectKey(GrantTableSubjectType.POST, SUBJECT_ID));
	}

	@Test
	@DisplayName("组织类全量覆盖：空角色列表仅删除 grant 行仍触发失效")
	void replaceOrgSubjectRolesClearsGrantsWhenRoleIdsEmpty() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		when(sysPostMapper.countEffectiveById(SUBJECT_ID)).thenReturn(1L);
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleIds(List.of());

		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.POST.name(), List.of(SUBJECT_ID));
		verify(grantTableService, never()).saveBatch(any(Collection.class));
		verify(grantInvalidationTrigger).submitByGrantSubjects(any(), eq("replace-roles"));
	}

	@Test
	@DisplayName("全量覆盖：先删后批量 insert 授权边")
	void replaceSubjectRoleGrantsDeletesThenInsertsEntities() {
		grantTableService.replaceSubjectRoleGrants("USER", SUBJECT_ID, List.of(101L, 102L));

		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.USER.name(), List.of(SUBJECT_ID));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<GrantTableEntity>> entitiesCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(grantTableService).saveBatch(entitiesCaptor.capture());
		assertThat(entitiesCaptor.getValue()).hasSize(2);
		assertThat(entitiesCaptor.getValue())
			.allSatisfy(entity -> assertThat(entity.getSubjectType()).isEqualTo("USER"))
			.allSatisfy(entity -> assertThat(entity.getSubjectId()).isEqualTo(SUBJECT_ID));
		assertThat(entitiesCaptor.getValue().stream().map(GrantTableEntity::getRoleId).toList())
			.containsExactlyInAnyOrder(101L, 102L);
	}

	@Test
	@DisplayName("全量覆盖：空角色列表仅删除不 insert")
	void replaceSubjectRoleGrantsDeletesOnlyWhenRoleIdsEmpty() {
		grantTableService.replaceSubjectRoleGrants("DEPT", SUBJECT_ID, List.of());

		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.DEPT.name(), List.of(SUBJECT_ID));
		verify(grantTableService, never()).saveBatch(any(Collection.class));
	}

}
