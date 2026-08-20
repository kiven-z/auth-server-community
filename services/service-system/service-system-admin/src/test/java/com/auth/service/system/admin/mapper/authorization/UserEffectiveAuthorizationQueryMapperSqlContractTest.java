package com.auth.service.system.admin.mapper.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：用户生效授权展开仅 USER 直授，不经视图/任职 UNION
 */
@DisplayName("UserEffectiveAuthorizationQueryMapper SQL 契约")
class UserEffectiveAuthorizationQueryMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = UserEffectiveAuthorizationQueryMapperSqlContractTest.class
			.getResourceAsStream("/mapper/authorization/UserEffectiveAuthorizationQueryMapper.xml")) {
			assertThat(in)
				.as("resource %s must exist", "/mapper/authorization/UserEffectiveAuthorizationQueryMapper.xml")
				.isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static String selectBlock(String xml, String id) {
		int start = xml.indexOf("<select id=\"" + id + "\"");
		assertThat(start).as(id).isNotNegative();
		int end = xml.indexOf("</select>", start);
		return xml.substring(start, end);
	}

	@Test
	@DisplayName("生效角色/权限：grant_table USER 直授，不含任职 UNION / 闭包 / 视图")
	void effectiveAuthorizationUsesUserDirectGrants() throws IOException {
		String xml = readClasspathText();

		assertThat(xml).doesNotContain("userSubjectsForFunctionalGrantByUserId")
			.doesNotContain("v_user_grant_subjects")
			.doesNotContain("UNION ALL")
			.doesNotContain("dept_closure")
			.doesNotContain("da.status = 1")
			.doesNotContain("grant.subject_id = ud.dept_id");

		assertThat(selectBlock(xml, "countPostsByUserId")).contains("v_user_post_effective");
		assertThat(selectBlock(xml, "countDeptsByUserId")).contains("v_user_dept_effective");
		assertThat(selectBlock(xml, "countEffectiveRolesByUserId")).contains("grant_table gt")
			.contains("subject_type = 'USER'")
			.contains("gt.subject_id = #{userId}");
		assertThat(selectBlock(xml, "selectEffectiveRolesByUserIdPage")).contains("grant_table gt")
			.contains("subject_type = 'USER'")
			.contains("gt.subject_id = #{userId}");
		assertThat(selectBlock(xml, "countEffectivePermissionsByUserId")).contains("grant_table gt")
			.contains("subject_type = 'USER'")
			.contains("gt.subject_id = #{userId}");
		assertThat(selectBlock(xml, "selectEffectivePermissionsByUserIdPage")).contains("grant_table gt")
			.contains("subject_type = 'USER'")
			.contains("gt.subject_id = #{userId}");
	}

}
