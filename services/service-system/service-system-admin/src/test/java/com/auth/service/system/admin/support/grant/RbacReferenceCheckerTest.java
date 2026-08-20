package com.auth.service.system.admin.support.grant;

import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link RbacReferenceChecker} 单元测试。
 */
@DisplayName("RbacReferenceChecker RBAC 关联校验")
@ExtendWith(MockitoExtension.class)
class RbacReferenceCheckerTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private SysRoleMapper sysRoleMapper;

	@InjectMocks
	private RbacReferenceChecker rbacReferenceChecker;

	@Test
	@DisplayName("requireActivePermissionIds：空列表跳过校验")
	void requireActivePermissionIdsSkipsWhenEmpty() {
		assertThatCode(() -> rbacReferenceChecker.requireActivePermissionIds(List.of())).doesNotThrowAnyException();

		verify(sysPermissionMapper, never()).selectActivePermissionIds(any());
	}

	@Test
	@DisplayName("requireActivePermissionIds：全部已启用时不抛异常")
	void requireActivePermissionIdsPassesWhenAllActive() {
		when(sysPermissionMapper.selectActivePermissionIds(List.of(10L, 20L))).thenReturn(List.of(10L, 20L));

		assertThatCode(() -> rbacReferenceChecker.requireActivePermissionIds(List.of(10L, 20L)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("requireActivePermissionIds：重复 ID 内部去重后校验通过")
	void requireActivePermissionIdsDeduplicatesBeforeCheck() {
		when(sysPermissionMapper.selectActivePermissionIds(List.of(10L))).thenReturn(List.of(10L));

		assertThatCode(() -> rbacReferenceChecker.requireActivePermissionIds(List.of(10L, 10L)))
			.doesNotThrowAnyException();

		verify(sysPermissionMapper).selectActivePermissionIds(List.of(10L));
	}

	@Test
	@DisplayName("requireActivePermissionIds：存在无效或未启用权限 ID 时抛出 GRANT_REFERENCE_INVALID 且不暴露 ID")
	void requireActivePermissionIdsThrowsWhenNotAllActive() {
		when(sysPermissionMapper.selectActivePermissionIds(List.of(99L))).thenReturn(List.of());

		ThrowingCallable executable = () -> rbacReferenceChecker.requireActivePermissionIds(List.of(99L));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class).satisfies(ex -> {
			SystemBusinessException biz = (SystemBusinessException) ex;
			assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
			assertThat(biz.getMessageArgs()).isEmpty();
		});
	}

	@Test
	@DisplayName("getExisting：角色 ID 为空时抛出 DATA_NOT_EXIST")
	void getExistingThrowsWhenNull() {
		when(sysRoleMapper.selectById(null)).thenReturn(null);

		ThrowingCallable executable = () -> rbacReferenceChecker.getExisting(null);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class).satisfies(ex -> {
			SystemBusinessException biz = (SystemBusinessException) ex;
			assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
			assertThat(biz.getMessageArgs()).isEmpty();
		});
	}

	@Test
	@DisplayName("getExisting：角色不存在时抛出 DATA_NOT_EXIST")
	void getExistingThrowsWhenMissing() {
		when(sysRoleMapper.selectById(99L)).thenReturn(null);

		ThrowingCallable executable = () -> rbacReferenceChecker.getExisting(99L);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class).satisfies(ex -> {
			SystemBusinessException biz = (SystemBusinessException) ex;
			assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
			assertThat(biz.getMessageArgs()).isEmpty();
		});
	}

	@Test
	@DisplayName("getExisting：角色存在时返回实体")
	void getExistingReturnsEntity() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectById(1L)).thenReturn(role);

		SysRoleEntity result = rbacReferenceChecker.getExisting(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getRoleCode()).isEqualTo("ADMIN");
	}

	@Test
	@DisplayName("requireExistingEnabledRoleIds：可指定结果码")
	void requireExistingEnabledRoleIdsUsesCustomResultCode() {
		when(sysRoleMapper.selectActiveRoleIds(List.of(1L))).thenReturn(List.of(1L));

		assertThatCode(() -> rbacReferenceChecker.requireExistingEnabledRoleIds(List.of(1L),
				SystemCommonResultCode.DATA_NOT_EXIST))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("requireExistingEnabledRoleIds：空列表跳过校验")
	void requireExistingEnabledRoleIdsSkipsWhenEmpty() {
		assertThatCode(() -> rbacReferenceChecker.requireExistingEnabledRoleIds(List.of(),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();

		verify(sysRoleMapper, never()).selectActiveRoleIds(any());
	}

	@Test
	@DisplayName("requireExistingEnabledRoleIds：全部 eligible 时不抛异常")
	void requireExistingEnabledRoleIdsPassesWhenAllEligible() {
		when(sysRoleMapper.selectActiveRoleIds(List.of(1L))).thenReturn(List.of(1L));

		assertThatCode(() -> rbacReferenceChecker.requireExistingEnabledRoleIds(List.of(1L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("requireExistingEnabledRoleIds：重复 ID 内部去重后校验通过")
	void requireExistingEnabledRoleIdsDeduplicatesBeforeCheck() {
		when(sysRoleMapper.selectActiveRoleIds(List.of(1L))).thenReturn(List.of(1L));

		assertThatCode(() -> rbacReferenceChecker.requireExistingEnabledRoleIds(List.of(1L, 1L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();

		verify(sysRoleMapper).selectActiveRoleIds(List.of(1L));
	}

	@Test
	@DisplayName("requireExistingEnabledRoleIds：存在无效角色时抛出指定结果码且不暴露 ID")
	void requireExistingEnabledRoleIdsThrowsWhenNotAllEligible() {
		when(sysRoleMapper.selectActiveRoleIds(List.of(99L))).thenReturn(List.of());

		ThrowingCallable executable = () -> rbacReferenceChecker.requireExistingEnabledRoleIds(List.of(99L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class).satisfies(ex -> {
			SystemBusinessException biz = (SystemBusinessException) ex;
			assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
			assertThat(biz.getMessageArgs()).isEmpty();
		});
	}

}
