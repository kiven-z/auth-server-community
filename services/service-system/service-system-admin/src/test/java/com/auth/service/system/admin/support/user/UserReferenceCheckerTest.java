package com.auth.service.system.admin.support.user;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.pipeline.authenticate.AuthProfileSecurityContextPopulator;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.po.user.UserBusinessKeyRowPO;
import com.auth.service.system.admin.model.po.user.UserBusinessKeysExisting;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link UserReferenceChecker} 单元测试
 */
@DisplayName("UserReferenceChecker 用户引用校验")
@ExtendWith(MockitoExtension.class)
class UserReferenceCheckerTest {

	private static final Long EXISTING_USER_ID = 100L;

	private static final Long SUPER_ADMIN_USER_ID = PermissionConstant.SUPER_ADMIN_USER_IDS.iterator().next();

	private final AuthProfileSecurityContextPopulator securityContextPopulator = new AuthProfileSecurityContextPopulator();

	@Mock
	private SysUserMapper sysUserMapper;

	@InjectMocks
	private UserReferenceChecker userReferenceChecker;

	@AfterEach
	void tearDownSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("getExistingActive：用户存在时返回实体")
	void getExistingActiveReturnsEntity() {
		UserEntity user = new UserEntity();
		user.setId(EXISTING_USER_ID);
		when(sysUserMapper.selectById(EXISTING_USER_ID)).thenReturn(user);

		UserEntity result = userReferenceChecker.getExistingActive(EXISTING_USER_ID);

		assertThat(result.getId()).isEqualTo(EXISTING_USER_ID);
	}

	@Test
	@DisplayName("getExistingActive：用户不存在时抛出 USER_NOT_FOUND")
	void getExistingActiveThrowsWhenMissing() {
		when(sysUserMapper.selectById(EXISTING_USER_ID)).thenReturn(null);

		SystemBusinessException exception = (SystemBusinessException) assertThatThrownBy(
				() -> userReferenceChecker.getExistingActive(EXISTING_USER_ID))
			.isInstanceOf(SystemBusinessException.class)
			.actual();
		assertThat(exception.getResultCode()).isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
		assertThat(exception.getMessageArgs()).isEmpty();
	}

	@Test
	@DisplayName("findExistingBusinessKeys：候选均为空时不查库")
	void findExistingBusinessKeysSkipsWhenAllCandidatesEmpty() {
		UserBusinessKeysExisting result = userReferenceChecker.findExistingBusinessKeys(Set.of(), Set.of(), Set.of(),
				Set.of(), null);

		assertThat(result).isEqualTo(UserBusinessKeysExisting.empty());
		verify(sysUserMapper, never()).selectRowsByBusinessKeys(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("findExistingBusinessKeys：按候选集合归类已占用业务键")
	void findExistingBusinessKeysClassifiesHitsByField() {
		UserBusinessKeyRowPO row = new UserBusinessKeyRowPO();
		row.setUsername("alice");
		row.setEmail("bob@example.com");
		when(sysUserMapper.selectRowsByBusinessKeys(any(), any(), any(), any(), isNull())).thenReturn(List.of(row));

		UserBusinessKeysExisting result = userReferenceChecker.findExistingBusinessKeys(Set.of("alice"),
				Set.of("bob@example.com"), Set.of("13800000001"), Set.of("E001"), null);

		assertThat(result.usernames()).containsExactly("alice");
		assertThat(result.emails()).containsExactly("bob@example.com");
		assertThat(result.phones()).isEmpty();
		assertThat(result.employeeNos()).isEmpty();
		verify(sysUserMapper, times(1)).selectRowsByBusinessKeys(any(), any(), any(), any(), isNull());
	}

	@Test
	@DisplayName("requireAbsentUserBusinessKeys：空列表跳过校验")
	void requireAbsentUserBusinessKeysSkipsWhenEmpty() {
		assertThatCode(() -> userReferenceChecker.requireAbsentUserBusinessKeys(List.of(), List.of(), List.of(),
				List.of(), null))
			.doesNotThrowAnyException();

		verify(sysUserMapper, never()).selectRowsByBusinessKeys(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("requireAbsentUserBusinessKeys：请求内用户名重复时抛出 PARAM_DUPLICATE")
	void requireAbsentUserBusinessKeysThrowsWhenUsernameDuplicatedInRequest() {
		ThrowingCallable executable = () -> userReferenceChecker.requireAbsentUserBusinessKeys(
				List.of("alice", "alice"), List.of("a@example.com", "b@example.com"),
				List.of("13800000001", "13800000002"), List.of(), null);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);

		verify(sysUserMapper, never()).selectRowsByBusinessKeys(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("requireAbsentUserBusinessKeys：库中已存在用户名时抛出 PARAM_DUPLICATE")
	void requireAbsentUserBusinessKeysThrowsWhenUsernameExistsInDb() {
		UserBusinessKeyRowPO existing = new UserBusinessKeyRowPO();
		existing.setUsername("alice");
		when(sysUserMapper.selectRowsByBusinessKeys(any(), any(), any(), any(), isNull()))
			.thenReturn(List.of(existing));

		ThrowingCallable executable = () -> userReferenceChecker.requireAbsentUserBusinessKeys(List.of("alice"),
				List.of("alice@example.com"), List.of("13800000000"), List.of(), null);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_DUPLICATE);
	}

	@Test
	@DisplayName("requireAbsentUserBusinessKeys：多个空工号不因请求内重复抛异常")
	void requireAbsentUserBusinessKeysIgnoresMultipleBlankEmployeeNos() {
		// 工号为可选字段，批量导入时多条记录可能均无工号；空值不参与唯一性校验
		when(sysUserMapper.selectRowsByBusinessKeys(any(), any(), any(), any(), isNull())).thenReturn(List.of());

		List<String> blankEmployeeNos = new ArrayList<>();
		blankEmployeeNos.add(null);
		blankEmployeeNos.add("");
		blankEmployeeNos.add("  ");
		blankEmployeeNos.add(null);

		assertThatCode(() -> userReferenceChecker.requireAbsentUserBusinessKeys(List.of("alice", "bob"),
				List.of("alice@example.com", "bob@example.com"), List.of("13800000001", "13800000002"),
				blankEmployeeNos, null))
			.doesNotThrowAnyException();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<String>> employeeNosCaptor = ArgumentCaptor.forClass(Set.class);
		verify(sysUserMapper).selectRowsByBusinessKeys(any(), any(), any(), employeeNosCaptor.capture(), isNull());
		assertThat(employeeNosCaptor.getValue()).isEmpty();
	}

	@Test
	@DisplayName("requireAbsentUserBusinessKeys：各字段均未占用时不抛异常")
	void requireAbsentUserBusinessKeysPassesWhenAllAbsent() {
		when(sysUserMapper.selectRowsByBusinessKeys(any(), any(), any(), any(), isNull())).thenReturn(List.of());

		assertThatCode(() -> userReferenceChecker.requireAbsentUserBusinessKeys(List.of("alice", "bob"),
				List.of("alice@example.com", "bob@example.com"), List.of("13800000001", "13800000002"),
				List.of("E001", "E002"), null))
			.doesNotThrowAnyException();

		verify(sysUserMapper, times(1)).selectRowsByBusinessKeys(any(), any(), any(), any(), isNull());
	}

	@Test
	@DisplayName("requireAbsentUserBusinessKeys：更新时传递 excludeUserId")
	void requireAbsentUserBusinessKeysPassesExcludeUserId() {
		when(sysUserMapper.selectRowsByBusinessKeys(any(), any(), any(), any(), eq(EXISTING_USER_ID)))
			.thenReturn(List.of());

		assertThatCode(() -> userReferenceChecker.requireAbsentUserBusinessKeys(List.of("alice"),
				List.of("alice@example.com"), List.of("13800000001"), List.of(), EXISTING_USER_ID))
			.doesNotThrowAnyException();

		ArgumentCaptor<Long> excludeCaptor = ArgumentCaptor.forClass(Long.class);
		verify(sysUserMapper).selectRowsByBusinessKeys(any(), any(), any(), any(), excludeCaptor.capture());
		assertThat(excludeCaptor.getValue()).isEqualTo(EXISTING_USER_ID);
	}

	@Test
	@DisplayName("requireOperable：普通用户且无登录上下文时不抛异常")
	void requireOperablePassesForNormalUserWithoutSecurityContext() {
		assertThatCode(() -> userReferenceChecker.requireOperable(List.of(EXISTING_USER_ID)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("requireOperable：当前用户与目标不同且非超级管理员时不抛异常")
	void requireOperablePassesWhenCurrentUserDiffersFromTarget() {
		securityContextPopulator.populate(AuthProfile.builder().userId(200L).username("admin").build());

		assertThatCode(() -> userReferenceChecker.requireOperable(List.of(EXISTING_USER_ID)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("requireOperable：超级管理员不可被操作")
	void requireOperableThrowsForSuperAdmin() {
		List<Long> userIds = List.of(SUPER_ADMIN_USER_ID);

		SystemBusinessException exception = (SystemBusinessException) assertThatThrownBy(
				() -> userReferenceChecker.requireOperable(userIds))
			.isInstanceOf(SystemBusinessException.class)
			.actual();
		assertThat(exception.getResultCode()).isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
		assertThat(exception.getMessageArgs()).isEmpty();
	}

	@Test
	@DisplayName("requireOperable：当前登录用户不可操作自身")
	void requireOperableThrowsWhenTargetIsCurrentUser() {
		securityContextPopulator.populate(AuthProfile.builder().userId(EXISTING_USER_ID).username("self").build());
		List<Long> userIds = List.of(EXISTING_USER_ID);

		SystemBusinessException exception = (SystemBusinessException) assertThatThrownBy(
				() -> userReferenceChecker.requireOperable(userIds))
			.isInstanceOf(SystemBusinessException.class)
			.actual();
		assertThat(exception.getResultCode()).isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
		assertThat(exception.getMessageArgs()).isEmpty();
	}

}
