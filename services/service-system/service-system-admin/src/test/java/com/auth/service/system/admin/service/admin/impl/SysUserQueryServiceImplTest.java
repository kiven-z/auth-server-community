package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.UserEffectiveAuthorizationQueryMapper;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserQueryServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysUserQueryServiceImpl 用户只读查询")
@ExtendWith(MockitoExtension.class)
class SysUserQueryServiceImplTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private UserEffectiveAuthorizationQueryMapper userEffectiveAuthorizationQueryMapper;

	@Mock
	private GrantBindingQueryMapper grantBindingQueryMapper;

	private SysUserQueryServiceImpl sysUserQueryService;

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserQueryService = new SysUserQueryServiceImpl(auditUserDisplayService, userReferenceChecker,
				userEffectiveAuthorizationQueryMapper, grantBindingQueryMapper);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserQueryService, sysUserMapper);
	}

	@Test
	@DisplayName("userId 为空时抛出 PARAM_REQUIRED")
	void getProfileRejectsNullUserId() {
		assertThatThrownBy(() -> sysUserQueryService.getProfile(null)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
	}

	@Test
	@DisplayName("用户不存在时抛出 USER_NOT_FOUND")
	void getProfileRejectsMissingUser() {
		when(sysUserMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysUserQueryService.getProfile(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("正常查询时返回档案标量与部门岗位计数")
	void getProfileReturnsAggregatedData() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("alice");
		user.setNickname("Alice");
		user.setEmail("a@example.com");
		user.setPhone("13800000000");
		user.setAvatar("https://example.com/a.png");
		user.setStatus(0);
		user.setGender(1);
		user.setBirthday(LocalDate.of(1990, 1, 2));
		user.setIntroduction("bio");

		when(sysUserMapper.selectById(1L)).thenReturn(user);
		when(userEffectiveAuthorizationQueryMapper.countDeptsByUserId(1L)).thenReturn(2L);
		when(userEffectiveAuthorizationQueryMapper.countPostsByUserId(1L)).thenReturn(1L);

		SysUserProfileVO profile = sysUserQueryService.getProfile(1L);

		assertThat(profile.getUsername()).isEqualTo("alice");
		assertThat(profile.getDeptCount()).isEqualTo(2L);
		assertThat(profile.getPostCount()).isEqualTo(1L);
		verify(userEffectiveAuthorizationQueryMapper).countDeptsByUserId(1L);
		verify(userEffectiveAuthorizationQueryMapper).countPostsByUserId(1L);
	}

	@Test
	@DisplayName("用户详情：档案计数外补齐直连与生效授权计数")
	void getDetailReturnsAggregatedCounts() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("alice");
		user.setNickname("Alice");
		user.setEmail("a@example.com");
		user.setPhone("13800000000");
		user.setEmployeeNo("E001");
		user.setAvatar("https://example.com/a.png");
		user.setStatus(1);
		user.setGender(1);
		user.setBirthday(LocalDate.of(1990, 1, 2));
		user.setIntroduction("bio");
		user.setRemark("note");

		when(sysUserMapper.selectById(1L)).thenReturn(user);
		when(userEffectiveAuthorizationQueryMapper.countDeptsByUserId(1L)).thenReturn(1L);
		when(userEffectiveAuthorizationQueryMapper.countPostsByUserId(1L)).thenReturn(1L);
		when(grantBindingQueryMapper.countBoundRolesBySubject(GrantTableSubjectType.USER.name(), 1L, null))
			.thenReturn(3L);
		when(userEffectiveAuthorizationQueryMapper.countEffectiveRolesByUserId(1L, null)).thenReturn(5L);
		when(userEffectiveAuthorizationQueryMapper.countEffectivePermissionsByUserId(1L, null)).thenReturn(12L);

		SysUserDetailVO detail = sysUserQueryService.getDetail(1L);

		assertThat(detail.getUsername()).isEqualTo("alice");
		assertThat(detail.getEmployeeNo()).isEqualTo("E001");
		assertThat(detail.getRemark()).isEqualTo("note");
		assertThat(detail.getDeptCount()).isEqualTo(1L);
		assertThat(detail.getPostCount()).isEqualTo(1L);
		assertThat(detail.getDirectRoleCount()).isEqualTo(3L);
		assertThat(detail.getEffectiveRoleCount()).isEqualTo(5L);
		assertThat(detail.getEffectivePermissionCount()).isEqualTo(12L);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("关键词为空时不查库并返回空列表")
	void searchReturnsEmptyWhenKeywordBlank() {
		assertThat(sysUserQueryService.searchByKeyword(null, 10)).isEmpty();
		assertThat(sysUserQueryService.searchByKeyword("   ", 10)).isEmpty();
		verifyNoInteractions(sysUserMapper);
	}

	@Test
	@DisplayName("limit 为 null 时默认使用 20")
	void searchUsesDefaultLimitWhenNull() {
		when(sysUserMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("alice", null);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserMapper).searchByKeyword(eq("alice"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(20);
	}

	@Test
	@DisplayName("limit 超过 50 时封顶为 50")
	void searchCapsLimitAtFifty() {
		when(sysUserMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("bob", 200);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserMapper).searchByKeyword(eq("bob"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(50);
	}

	@Test
	@DisplayName("limit 小于 1 时提升为 1")
	void searchRaisesTinyLimitToOne() {
		when(sysUserMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("c", 0);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserMapper).searchByKeyword(eq("c"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(1);
	}

}
