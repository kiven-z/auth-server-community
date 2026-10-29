package com.auth.service.system.admin.support.user;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.mapper.admin.user.UserDeptMapper;
import com.auth.service.system.admin.model.po.user.UserPrimaryDeptPO;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.common.service.AuditUserDisplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link UserDetailSupport} 单元测试
 *
 * @author Bunny
 */
@DisplayName("UserDetailSupport 用户详情组装")
@ExtendWith(MockitoExtension.class)
class UserDetailSupportTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private UserDeptMapper userDeptMapper;

	private UserDetailSupport userDetailSupport;

	@BeforeEach
	void setUp() {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		userDetailSupport = new UserDetailSupport(auditUserDisplayService, userReferenceChecker, userDeptMapper);
	}

	@Test
	@DisplayName("用户详情：个人字段与主部门一并填充")
	void getDetailReturnsPersonalFieldsAndPrimaryDept() {
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

		UserPrimaryDeptPO primaryDept = new UserPrimaryDeptPO();
		primaryDept.setDeptId(20L);
		primaryDept.setDeptName("研发部");

		when(sysUserMapper.selectById(1L)).thenReturn(user);
		when(userDeptMapper.selectPrimaryDeptByUserId(1L)).thenReturn(primaryDept);

		SysUserDetailVO detail = userDetailSupport.getDetail(1L);

		assertThat(detail.getUsername()).isEqualTo("alice");
		assertThat(detail.getEmployeeNo()).isEqualTo("E001");
		assertThat(detail.getGender()).isEqualTo(1);
		assertThat(detail.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 2));
		assertThat(detail.getIntroduction()).isEqualTo("bio");
		assertThat(detail.getRemark()).isEqualTo("note");
		assertThat(detail.getPrimaryDeptId()).isEqualTo(20L);
		assertThat(detail.getPrimaryDeptName()).isEqualTo("研发部");
		verify(userDeptMapper).selectPrimaryDeptByUserId(1L);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

}
