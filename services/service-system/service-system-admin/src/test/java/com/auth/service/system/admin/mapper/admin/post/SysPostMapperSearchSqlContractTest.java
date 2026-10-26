package com.auth.service.system.admin.mapper.admin.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位搜索打基表，不按启用状态过滤
 *
 * @author Bunny
 */
@DisplayName("SysPostMapper search SQL 契约")
class SysPostMapperSearchSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = SysPostMapperSearchSqlContractTest.class
			.getResourceAsStream("/mapper/admin/post/SysPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/post/SysPostMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("搜索打 sys_post，不按启用状态或有效视图过滤")
	void searchUsesBaseTableWithoutStatusFilter() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"search\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("sys_post p")
			.doesNotContain("v_dept_effective")
			.doesNotContain("v_post_effective")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("p.status")
			.doesNotContain("test=\"status");
	}

}
