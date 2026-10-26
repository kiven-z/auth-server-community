package com.auth.service.system.admin.mapper.admin.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：用户部门关联打基表
 *
 * @author Bunny
 */
@DisplayName("UserDeptMapper SQL 契约")
class UserDeptMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = UserDeptMapperSqlContractTest.class
			.getResourceAsStream("/mapper/admin/user/UserDeptMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/user/UserDeptMapper.xml").isNotNull();
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
	@DisplayName("用户部门分页与主部门：打 user_dept，不走有效视图")
	void userDeptQueriesUseBaseTable() throws IOException {
		String xml = readClasspathText();

		assertThat(selectBlock(xml, "selectListByPage")).contains("user_dept ud")
			.contains("sys_dept d")
			.doesNotContain("dept_status")
			.doesNotContain("dept_effective")
			.doesNotContain("v_dept_effective")
			.doesNotContain("v_post_effective")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective");
		assertThat(selectBlock(xml, "selectByIdAndUserId")).contains("FROM user_dept")
			.doesNotContain("v_user_dept_effective");
		assertThat(selectBlock(xml, "selectPrimaryDeptByUserId")).contains("user_dept ud")
			.contains("sys_dept d")
			.doesNotContain("v_dept_effective")
			.doesNotContain("v_post_effective")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective");
	}

}
