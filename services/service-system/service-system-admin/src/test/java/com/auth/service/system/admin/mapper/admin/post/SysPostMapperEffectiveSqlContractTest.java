package com.auth.service.system.admin.mapper.admin.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位列表打基表
 *
 * @author Bunny
 */
@DisplayName("SysPostMapper 列表 SQL 契约")
class SysPostMapperEffectiveSqlContractTest {

	private static String readClasspathXml() throws IOException {
		try (InputStream in = SysPostMapperEffectiveSqlContractTest.class
			.getResourceAsStream("/mapper/admin/post/SysPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/post/SysPostMapper.xml").isNotNull();
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
	@DisplayName("分页列表：打 sys_post，不投影启用或计算有效")
	void pageQueryUsesBaseTable() throws IOException {
		String xml = readClasspathXml();

		assertThat(selectBlock(xml, "selectListByPage")).contains("sys_post p")
			.doesNotContain("v_dept_effective")
			.doesNotContain("v_post_effective")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("AS effective")
			.doesNotContain("p.status");
	}

}
