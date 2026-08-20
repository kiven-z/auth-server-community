package com.auth.service.system.admin.mapper.admin.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位列表带计算有效
 *
 * @author Bunny
 */
@DisplayName("SysPostMapper 计算有效 SQL 契约")
class SysPostMapperEffectiveSqlContractTest {

	private static String readClasspathXml() throws IOException {
		try (InputStream in = SysPostMapperEffectiveSqlContractTest.class
			.getResourceAsStream("/mapper/admin/post/SysPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/post/SysPostMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static String selectBlock(String xml) {
		int start = xml.indexOf("<select id=\"" + "selectListByPage" + "\"");
		assertThat(start).as("selectListByPage").isNotNegative();
		int end = xml.indexOf("</select>", start);
		return xml.substring(start, end);
	}

	@Test
	@DisplayName("分页列表：LEFT JOIN v_post_effective 投影 effective")
	void pageQueryProjectsEffectiveFromView() throws IOException {
		String xml = readClasspathXml();

		assertThat(selectBlock(xml)).contains("v_post_effective vpe").contains("AS effective").contains("sys_post p");
	}

}
