package com.auth.service.system.admin.mapper.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位授权面绑定用户打基表
 *
 * @author Bunny
 */
@DisplayName("PostRelationQueryMapper SQL 契约")
class PostRelationQueryMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = PostRelationQueryMapperSqlContractTest.class
			.getResourceAsStream("/mapper/authorization/PostRelationQueryMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/authorization/PostRelationQueryMapper.xml")
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
	@DisplayName("绑定用户 count/page：打 user_post 基表，不走有效任职视图")
	void boundUsersQueryBaseTableNotEffectiveView() throws IOException {
		String xml = readClasspathText();

		assertThat(selectBlock(xml, "countUsersByPostId")).contains("user_post up")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("v_post_effective");
		assertThat(selectBlock(xml, "selectUsersByPostIdPage")).contains("user_post up")
			.contains("up.created_at")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("v_post_effective");
	}

}
