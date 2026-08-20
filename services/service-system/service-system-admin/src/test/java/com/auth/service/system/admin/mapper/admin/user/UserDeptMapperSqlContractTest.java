package com.auth.service.system.admin.mapper.admin.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：管理端用户部门列表打基表；C 端主部门走有效任职
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
	@DisplayName("用户部门分页：基表全量；主部门查询走有效任职")
	void userDeptPageUsesBaseTableAndPrimaryUsesEffectiveView() throws IOException {
		String xml = readClasspathText();

		assertThat(selectBlock(xml, "selectListByPage")).contains("user_dept ud")
			.contains("dept_status")
			.contains("dept_effective")
			.contains("v_dept_effective")
			.doesNotContain("v_user_dept_effective");
		assertThat(selectBlock(xml, "selectByIdAndUserId")).contains("FROM user_dept")
			.doesNotContain("v_user_dept_effective");
		assertThat(selectBlock(xml, "selectPrimaryDeptByUserId")).contains("v_user_dept_effective")
			.doesNotContain("FROM user_dept");
	}

}
