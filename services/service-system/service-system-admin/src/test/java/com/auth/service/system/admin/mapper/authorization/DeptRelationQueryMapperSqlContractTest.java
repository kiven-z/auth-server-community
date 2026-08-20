package com.auth.service.system.admin.mapper.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：部门授权面绑定用户打基表
 *
 * @author Bunny
 */
@DisplayName("DeptRelationQueryMapper SQL 契约")
class DeptRelationQueryMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = DeptRelationQueryMapperSqlContractTest.class
			.getResourceAsStream("/mapper/authorization/DeptRelationQueryMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/authorization/DeptRelationQueryMapper.xml")
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
	@DisplayName("绑定用户 count/page：打 user_dept 基表，不走有效任职视图")
	void boundUsersQueryBaseTableNotEffectiveView() throws IOException {
		String xml = readClasspathText();

		assertThat(selectBlock(xml, "countUsersByDeptId")).contains("user_dept ud")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_dept_effective");
		assertThat(selectBlock(xml, "selectUsersByDeptIdPage")).contains("user_dept ud")
			.contains("ud.created_at")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_dept_effective");
	}

}
