package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import com.auth.service.system.admin.support.grant.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.grant.TypedGrantTableActiveSubjectChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
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
import static org.mockito.Mockito.*;

/**
 * {@link GrantTableServiceImpl} 单元测试
 */
@DisplayName("GrantTableServiceImpl grant_table 读写与授权编排")
@ExtendWith(MockitoExtension.class)
class GrantTableServiceImplTest {

	private static final Long SUBJECT_ID = 10L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	private GrantTableServiceImpl grantTableService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SUBJECT_ID);
		entity.setUsername("alice");
		entity.setStatus(1);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		TypedGrantTableActiveSubjectChecker userSubjectChecker = new TypedGrantTableActiveSubjectChecker(
				GrantTableSubjectType.USER, userReferenceChecker::getExistingActive);
		GrantTableSubjectExistenceVerifier subjectExistenceVerifier = new GrantTableSubjectExistenceVerifier(
				List.of(userSubjectChecker));
		grantTableService = spy(new GrantTableServiceImpl(subjectExistenceVerifier));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(grantTableService, grantTableMapper);
		lenient().doReturn(true).when(grantTableService).saveBatch(any(Collection.class));
	}

	@Test
	@DisplayName("查询已分配角色：主体存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenSubjectExists() {
		when(sysUserMapper.selectById(SUBJECT_ID)).thenReturn(activeUser());
		RoleReferencePO boundRole = new RoleReferencePO();
		boundRole.setId(101L);
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");
		boundRole.setStatus(true);
		when(grantTableMapper.selectAssignedRolesBySubject(GrantTableSubjectType.USER.name(), SUBJECT_ID))
			.thenReturn(List.of(boundRole));

		List<RoleReferenceVO> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.USER, SUBJECT_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getId()).isEqualTo(101L);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务角色A");
	}

	@Test
	@DisplayName("查询已分配角色：主体不存在时抛业务异常")
	void listAssignedRolesThrowsWhenSubjectMissing() {
		when(sysUserMapper.selectById(SUBJECT_ID)).thenReturn(null);

		assertThatThrownBy(() -> grantTableService.listAssignedRoles(GrantTableSubjectType.USER, SUBJECT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
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
		grantTableService.replaceSubjectRoleGrants("USER", SUBJECT_ID, List.of());

		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.USER.name(), List.of(SUBJECT_ID));
		verify(grantTableService, never()).saveBatch(any(Collection.class));
	}

}
