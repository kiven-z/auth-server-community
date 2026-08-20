package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.support.user.UserAvatarUpdateSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserServiceImpl} 单元测试
 */
@DisplayName("SysUserServiceImpl 用户管理")
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

	private static final Long USER_ID = 100L;

	private static final Long USER_ID_2 = 101L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	@Mock
	private UserSessionRevocationTrigger userSessionRevocationTrigger;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private UserAvatarUpdateSupport userAvatarUpdateSupport;

	private SysUserServiceImpl sysUserService;

	private static SysUserForm buildCreateForm() {
		SysUserForm form = new SysUserForm();
		form.setUsername("alice");
		form.setNickname("Alice");
		form.setEmail("alice@example.com");
		form.setPhone("13800000000");
		form.setStatus(1);
		form.setInitialPassword("SecurePwd1");
		form.setBirthday(LocalDate.of(1990, 5, 1));
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysUserService = spy(new SysUserServiceImpl(grantTableMapper, userReferenceChecker, userAvatarUpdateSupport,
				userAuthorizationInvalidationTrigger, userSessionRevocationTrigger, passwordEncoder));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserService, sysUserMapper);
		lenient().doReturn(true).when(sysUserService).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增用户：空列表直接返回")
	void createBatchFromImportSkipsWhenEmpty() {
		sysUserService.createBatchFromImport(List.of());

		verifyNoInteractions(userReferenceChecker);
		verify(sysUserService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增用户：使用表单初始密码编码入库并设置 permVersion")
	void createBatchFromImportEncodesInitialPasswordFromForm() {
		// 验证创建流程使用表单密码而非硬编码默认值
		SysUserForm form = buildCreateForm();
		when(passwordEncoder.encode("SecurePwd1")).thenReturn("$2a$encoded");

		sysUserService.createBatchFromImport(List.of(form));

		verify(userReferenceChecker).requireAbsentUserBusinessKeys(eq(List.of("alice")),
				eq(List.of("alice@example.com")), eq(List.of("13800000000")), anyList(), eq(null));
		ArgumentCaptor<List<UserEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(sysUserService).saveBatch(captor.capture(), eq(BatchSizes.SIZE_500));
		UserEntity entity = captor.getValue().get(0);
		assertThat(entity.getPassword()).isEqualTo("$2a$encoded");
		assertThat(entity.getPermVersion()).isZero();
		assertThat(entity.getAge()).isPositive();
		verify(passwordEncoder).encode("SecurePwd1");
	}

	@Test
	@DisplayName("批量新增用户：用户名重复时抛出业务异常")
	void createBatchFromImportDuplicateUsernameThrows() {
		SysUserForm form = buildCreateForm();
		doThrow(new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "用户名", form.getUsername()))
			.when(userReferenceChecker)
			.requireAbsentUserBusinessKeys(eq(List.of("alice")), eq(List.of("alice@example.com")),
					eq(List.of("13800000000")), anyList(), eq(null));

		ThrowingCallable executable = () -> sysUserService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);

		verify(sysUserService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("更新用户：从表单主键加载并持久化资料")
	void updateAppliesFormById() {
		SysUserForm form = buildCreateForm();
		form.setId(USER_ID);
		UserEntity existing = new UserEntity();
		existing.setId(USER_ID);
		existing.setUsername("alice");
		existing.setEmail("alice@example.com");
		existing.setPhone("13800000000");
		existing.setStatus(1);
		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		lenient().doReturn(true).when(sysUserService).updateById(existing);

		sysUserService.update(form);

		verify(userReferenceChecker).getExistingActive(USER_ID);
		verify(userReferenceChecker).requireOperable(List.of(USER_ID));
		verify(userReferenceChecker).requireAbsentUserBusinessKeys(eq(List.of("alice")),
				eq(List.of("alice@example.com")), eq(List.of("13800000000")), anyList(), eq(USER_ID));
		verify(sysUserService).updateById(existing);
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
		verify(userSessionRevocationTrigger, never()).revokeAllSessionsAfterCommit(any());
	}

	@Test
	@DisplayName("更新用户：状态变更时提交授权失效并踢出会话")
	void updateStatusChangeSubmitsInvalidationAndRevokesSessions() {
		SysUserForm form = buildCreateForm();
		form.setId(USER_ID);
		form.setStatus(0);
		UserEntity existing = new UserEntity();
		existing.setId(USER_ID);
		existing.setUsername("alice");
		existing.setEmail("alice@example.com");
		existing.setPhone("13800000000");
		existing.setStatus(1);
		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		lenient().doReturn(true).when(sysUserService).updateById(existing);

		sysUserService.update(form);

		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "update");
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(List.of(USER_ID));
	}

	@Test
	@DisplayName("更新用户：身份字段变更时提交授权失效并踢出会话")
	void updateIdentityChangeSubmitsInvalidationAndRevokesSessions() {
		SysUserForm form = buildCreateForm();
		form.setId(USER_ID);
		form.setEmail("alice-new@example.com");
		UserEntity existing = new UserEntity();
		existing.setId(USER_ID);
		existing.setUsername("alice");
		existing.setEmail("alice@example.com");
		existing.setPhone("13800000000");
		existing.setStatus(1);
		when(userReferenceChecker.getExistingActive(USER_ID)).thenReturn(existing);
		lenient().doReturn(true).when(sysUserService).updateById(existing);

		sysUserService.update(form);

		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "update");
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(List.of(USER_ID));
	}

	@Test
	@DisplayName("批量删除：提交失效、清理 grant_table 后物理删除并踢出会话")
	void deleteByIdsPurgesGrantTableAndPhysicallyDeletes() {
		List<Long> ids = List.of(USER_ID);

		sysUserService.deleteByIds(ids);

		verify(userReferenceChecker).requireOperable(ids);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(ids, "delete");
		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.USER.name(), ids);
		verify(sysUserMapper).deleteByIds(ids);
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(ids);
	}

	@Test
	@DisplayName("批量删除：多个用户仅调用一次 grant_table 批量清理")
	void deleteByIdsPurgesGrantTableInSingleBatchForMultipleUsers() {
		List<Long> ids = List.of(USER_ID, USER_ID_2);

		sysUserService.deleteByIds(ids);

		verify(userReferenceChecker).requireOperable(ids);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(ids, "delete");
		verify(grantTableMapper).deleteBySubjectIds(GrantTableSubjectType.USER.name(), ids);
		verify(sysUserMapper).deleteByIds(ids);
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(ids);
	}

	@Test
	@DisplayName("批量启停：提交授权失效并踢出会话")
	void batchUpdateStatusSubmitsInvalidationAndRevokesSessions() {
		SysUserBatchStatusForm form = new SysUserBatchStatusForm();
		form.setIds(List.of(USER_ID));
		form.setStatus(0);
		lenient().doReturn(true).when(sysUserService).updateBatchById(anyList());

		sysUserService.batchUpdateStatus(form);

		verify(userReferenceChecker).requireOperable(List.of(USER_ID));
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "update");
		verify(userSessionRevocationTrigger).revokeAllSessionsAfterCommit(List.of(USER_ID));
	}

}
