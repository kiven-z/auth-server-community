package com.auth.service.system.admin.mapper.admin.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：用户岗位关联打基表
 *
 * @author Bunny
 */
@DisplayName("UserPostMapper SQL 契约")
class UserPostMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = UserPostMapperSqlContractTest.class
			.getResourceAsStream("/mapper/admin/user/UserPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/user/UserPostMapper.xml").isNotNull();
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
	@DisplayName("用户岗位分页：打 user_post，不投影启用或计算有效")
	void userPostPageUsesBaseTable() throws IOException {
		String xml = readClasspathText();

		assertThat(selectBlock(xml, "selectListByPage")).contains("user_post up")
			.contains("sys_post sp")
			.doesNotContain("post_status")
			.doesNotContain("post_effective")
			.doesNotContain("v_dept_effective")
			.doesNotContain("v_post_effective")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective");
		assertThat(selectBlock(xml, "selectByIdAndUserId")).contains("FROM user_post")
			.doesNotContain("v_user_post_effective");
	}

}
