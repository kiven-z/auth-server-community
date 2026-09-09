package com.auth.service.auth.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：正向主体展开走 v_user_grant_subjects，不在 Mapper 重复 UNION
 */
@DisplayName("UserAuthorizationGrantMapper SQL 契约")
class UserAuthorizationGrantMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = UserAuthorizationGrantMapperSqlContractTest.class
			.getResourceAsStream("/mapper/UserAuthorizationGrantMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/UserAuthorizationGrantMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("正向主体：JOIN v_user_grant_subjects，不含任职 UNION / 闭包")
	void forwardSubjectsJoinGrantSubjectsView() throws IOException {
		String xml = readClasspathText();

		assertThat(xml).contains("selectRoleRowsByUserIds")
			.contains("selectPermissionRowsByUserIds")
			.contains("v_user_grant_subjects")
			.contains("user_id IN")
			.doesNotContain("AuthorizationGrantForwardFragments")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("dept_closure")
			.doesNotContain("da.status = 1")
			.doesNotContain("UNION ALL")
			.doesNotContain("grant.subject_id = ud.dept_id");
	}

}
