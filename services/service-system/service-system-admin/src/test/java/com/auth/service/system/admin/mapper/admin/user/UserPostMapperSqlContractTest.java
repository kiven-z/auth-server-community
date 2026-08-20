package com.auth.service.system.admin.mapper.admin.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：管理端用户岗位列表打基表并投影状态
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
	@DisplayName("用户岗位分页：基表全量；更新加载打基表")
	void userPostPageUsesBaseTableWithStatusProjection() throws IOException {
		String xml = readClasspathText();

		assertThat(selectBlock(xml, "selectListByPage")).contains("user_post up")
			.contains("post_status")
			.contains("post_effective")
			.contains("v_post_effective")
			.doesNotContain("v_user_post_effective");
		assertThat(selectBlock(xml, "selectByIdAndUserId")).contains("FROM user_post")
			.doesNotContain("v_user_post_effective");
	}

}
