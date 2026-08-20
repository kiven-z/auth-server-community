package com.auth.module.security.contract.convention;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AuthCodeConvention} 格式校验单测
 *
 * @author Bunny
 */
@DisplayName("AuthCodeConvention 编码约定")
class AuthCodeConventionTest {

	@Test
	@DisplayName("权限码：通配四种与业务三段、四段均合法")
	void permissionWellFormedSamples() {
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("*")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("*:*")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("*:*:*")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("*:*:*:*")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("sys")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("sys:dept")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("sys:dept:query")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("sys:dept:detail")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedPermissionCode("sys:file:recycle:query")).isTrue();
	}

	@Test
	@DisplayName("权限码：null 非法")
	void permissionNullIsFalse() {
		assertThat(AuthCodeConvention.isWellFormedPermissionCode(null)).isFalse();
	}

	@DisplayName("权限码：非法样例")
	@ParameterizedTest
	@ValueSource(strings = { "", "   ", "Sys:dept:query", "sys:dept:Query", "sys:file:recycle:query:extra", "9ab", "_x",
			"sys:dept:", ":query", "sys dept:query" })
	void permissionMalformed(String code) {
		assertThat(AuthCodeConvention.isWellFormedPermissionCode(code.trim())).isFalse();
	}

	@Test
	@DisplayName("角色码：合法样例")
	void roleWellFormedSamples() {
		assertThat(AuthCodeConvention.isWellFormedRoleCode("ADMIN")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedRoleCode("SYS_ADMIN")).isTrue();
		assertThat(AuthCodeConvention.isWellFormedRoleCode("R_DEMO")).isTrue();
	}

	@Test
	@DisplayName("角色码：null 非法")
	void roleNullIsFalse() {
		assertThat(AuthCodeConvention.isWellFormedRoleCode(null)).isFalse();
	}

	@DisplayName("角色码：非法样例")
	@ParameterizedTest
	@ValueSource(strings = { "", "admin", "Admin", "SYS-ADMIN", "SYS ADMIN", "1ADMIN", "ROLE_1" })
	void roleMalformed(String code) {
		assertThat(AuthCodeConvention.isWellFormedRoleCode(code)).isFalse();
	}

}
