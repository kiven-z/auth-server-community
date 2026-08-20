package com.auth.service.auth.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：正向授权展开仅 USER 直授，不经视图/任职 UNION
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
	@DisplayName("正向主体：grant_table USER 直授，不含任职 UNION / 闭包 / 视图")
	void forwardSubjectsUseUserDirectGrants() throws IOException {
		String xml = readClasspathText();

		assertThat(xml).contains("selectRoleRowsByUserIds")
			.contains("selectPermissionRowsByUserIds")
			.contains("grant_table gt")
			.contains("subject_type = 'USER'")
			.doesNotContain("v_user_grant_subjects")
			.doesNotContain("AuthorizationGrantForwardFragments")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("dept_closure")
			.doesNotContain("da.status = 1")
			.doesNotContain("UNION ALL")
			.doesNotContain("grant.subject_id = ud.dept_id");
	}

}
