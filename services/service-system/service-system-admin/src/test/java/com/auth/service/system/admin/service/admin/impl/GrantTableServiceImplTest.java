package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.security.contract.api.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.form.role.SysRoleGrantSubjectsForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.support.grant.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.grant.TypedGrantTableActiveSubjectChecker;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
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

	private static final Long ROLE_ID = 1L;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private RbacReferenceChecker rbacReferenceChecker;

	@Mock
	private GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	private GrantTableServiceImpl grantTableService;

	private static SysPostEntity activePost() {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(GrantTableServiceImplTest.SUBJECT_ID);
		entity.setDeptId(1L);
		entity.setPostCode("DEV");
		entity.setPostName("开发岗");
		entity.setStatus(true);
		return entity;
	}

	private static SysRoleGrantSubjectsForm subjectsForm(GrantTableSubjectType subjectType, List<Long> subjectIds) {
		SysRoleGrantSubjectsForm form = new SysRoleGrantSubjectsForm();
		form.setSubjectType(subjectType);
		form.setSubjectIds(subjectIds);
		return form;
	}

	/**
	 * 构造含 USER/DEPT/POST 校验的 Verifier（测试用；校验回调为空操作）
	 * @return 主体存在性校验器
	 */
	private static GrantTableSubjectExistenceVerifier buildVerifierWithMocks() {
		return new GrantTableSubjectExistenceVerifier(
				List.of(new TypedGrantTableActiveSubjectChecker(GrantTableSubjectType.USER, subjectId -> {
				}), new TypedGrantTableActiveSubjectChecker(GrantTableSubjectType.DEPT, subjectId -> {
				}), new TypedGrantTableActiveSubjectChecker(GrantTableSubjectType.POST, subjectId -> {
				})));
	}

	private static void injectBaseMapper(GrantTableServiceImpl service, GrantTableMapper mapper) {
		try {
			Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
			baseMapperField.setAccessible(true);
			baseMapperField.set(service, mapper);
		}
		catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@BeforeEach
	void setUp() throws Exception {
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper);
		TypedGrantTableActiveSubjectChecker postSubjectChecker = new TypedGrantTableActiveSubjectChecker(
				GrantTableSubjectType.POST, postReferenceChecker::requireEffective);
		GrantTableSubjectExistenceVerifier subjectExistenceVerifier = new GrantTableSubjectExistenceVerifier(
				List.of(postSubjectChecker));
		grantTableService = spy(new GrantTableServiceImpl(subjectExistenceVerifier, userReferenceChecker,
				rbacReferenceChecker, grantInvalidationTrigger));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(grantTableService, grantTableMapper);
		lenient().doReturn(true).when(grantTableService).saveBatch(any());
		lenient().when(sysPostMapper.countEffectiveById(SUBJECT_ID)).thenReturn(1L);
	}

	@Test
	@DisplayName("查询已分配角色：主体存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenSubjectExists() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
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
	@DisplayName("查询已分配角色：包含非主体授权类型角色时仍返回")
	void listAssignedRolesReturnsLinkedRoleRegardlessOfType() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		RoleReferencePO menuRole = new RoleReferencePO();
		menuRole.setId(102L);
		menuRole.setRoleCode("MENU_ADMIN");
		menuRole.setRoleName("菜单管理员");
		menuRole.setStatus(true);
		when(grantTableMapper.selectAssignedRolesBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID))
			.thenReturn(List.of(menuRole));

		List<RoleReferenceVO> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.POST, SUBJECT_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getId()).isEqualTo(102L);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("MENU_ADMIN");
		assertThat(rows.get(0).getRoleName()).isEqualTo("菜单管理员");
	}

	@Test
	void listAssignedRolesThrowsWhenSubjectMissing() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(null);

		assertThatThrownBy(() -> grantTableService.listAssignedRoles(GrantTableSubjectType.POST, SUBJECT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("角色视角按类型覆盖：删插并失效旧∪新主体，不影响其它类型")
	void replaceGrantSubjectsByRoleIdReplacesAndInvalidatesUnion() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(null);
		when(grantTableMapper.selectSubjectIdsByRoleIdAndSubjectType(ROLE_ID, "USER")).thenReturn(List.of(11L));

		GrantTableSubjectExistenceVerifier verifierWithUserDeptPost = buildVerifierWithMocks();
		grantTableService = spy(new GrantTableServiceImpl(verifierWithUserDeptPost, userReferenceChecker,
				rbacReferenceChecker, grantInvalidationTrigger));
		injectBaseMapper(grantTableService, grantTableMapper);
		doReturn(true).when(grantTableService).saveBatch(any());

		grantTableService.replaceGrantSubjectsByRoleId(ROLE_ID, subjectsForm(GrantTableSubjectType.USER, List.of(12L)));

		verify(userReferenceChecker).requireOperable(List.of(12L));
		verify(grantTableMapper).deleteByRoleIdAndSubjectType(ROLE_ID, "USER");
		verify(grantTableMapper, never()).deleteByRoleIdAndSubjectType(ROLE_ID, "DEPT");
		verify(grantTableMapper, never()).deleteByRoleIdAndSubjectType(ROLE_ID, "POST");

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<GrantSubjectKey>> subjectsCaptor = ArgumentCaptor.forClass(List.class);
		verify(grantInvalidationTrigger).submitByGrantSubjects(subjectsCaptor.capture(), eq("replace-grant-subjects"));
		assertThat(subjectsCaptor.getValue()).containsExactlyInAnyOrder(
				new GrantSubjectKey(GrantTableSubjectType.USER, 11L),
				new GrantSubjectKey(GrantTableSubjectType.USER, 12L));
	}

	@Test
	@DisplayName("角色视角按类型覆盖：空列表仅清空该类型并失效旧主体")
	void replaceGrantSubjectsByRoleIdClearsWhenListsEmpty() {
		when(rbacReferenceChecker.getExisting(ROLE_ID)).thenReturn(null);
		when(grantTableMapper.selectSubjectIdsByRoleIdAndSubjectType(ROLE_ID, "DEPT")).thenReturn(List.of(21L));

		GrantTableSubjectExistenceVerifier verifierWithUserDeptPost = buildVerifierWithMocks();
		grantTableService = spy(new GrantTableServiceImpl(verifierWithUserDeptPost, userReferenceChecker,
				rbacReferenceChecker, grantInvalidationTrigger));
		injectBaseMapper(grantTableService, grantTableMapper);

		grantTableService.replaceGrantSubjectsByRoleId(ROLE_ID, subjectsForm(GrantTableSubjectType.DEPT, List.of()));

		verify(userReferenceChecker, never()).requireOperable(any());
		verify(grantTableService, never()).saveBatch(any());
		verify(grantTableMapper).deleteByRoleIdAndSubjectType(ROLE_ID, "DEPT");
		verify(grantTableMapper, never()).deleteByRoleIdAndSubjectType(ROLE_ID, "USER");
		verify(grantTableMapper, never()).deleteByRoleIdAndSubjectType(ROLE_ID, "POST");

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<GrantSubjectKey>> subjectsCaptor = ArgumentCaptor.forClass(List.class);
		verify(grantInvalidationTrigger).submitByGrantSubjects(subjectsCaptor.capture(), eq("replace-grant-subjects"));
		assertThat(subjectsCaptor.getValue()).containsExactly(new GrantSubjectKey(GrantTableSubjectType.DEPT, 21L));
	}

	@Test
	@DisplayName("角色视角按类型覆盖：角色不存在时不写库")
	void replaceGrantSubjectsByRoleIdThrowsWhenRoleMissing() {
		when(rbacReferenceChecker.getExisting(ROLE_ID))
			.thenThrow(new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST));
		SysRoleGrantSubjectsForm form = subjectsForm(GrantTableSubjectType.USER, List.of());

		assertThatThrownBy(() -> grantTableService.replaceGrantSubjectsByRoleId(ROLE_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
		verify(grantTableMapper, never()).deleteByRoleIdAndSubjectType(any(), any());
		verifyNoInteractions(grantInvalidationTrigger);
	}

	@Test
	@DisplayName("组织类全量覆盖：校验、写入 grant_table 并提交授权边失效")
	void replaceOrgSubjectRolesWritesGrantAndSubmitsInvalidation() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleIds(List.of(101L, 102L));

		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(rbacReferenceChecker).requireExistingEnabledRoleIds(List.of(101L, 102L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.POST.name(), List.of(SUBJECT_ID));
		verify(grantTableService).saveBatch(any());

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
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleIds(List.of());

		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.POST.name(), List.of(SUBJECT_ID));
		verify(grantTableService, never()).saveBatch(any());
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
		verify(grantTableService, never()).saveBatch(any());
	}

}
