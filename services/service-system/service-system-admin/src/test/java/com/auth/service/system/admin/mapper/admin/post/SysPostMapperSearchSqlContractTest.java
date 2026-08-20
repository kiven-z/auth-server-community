package com.auth.service.system.admin.mapper.admin.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：启用搜索仅返回计算有效岗位
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
	@DisplayName("status=true 走 v_post_effective；status=false 打基表 status")
	void searchEnabledUsesEffectiveView() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"search\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("v_post_effective")
			.contains("status != null and status")
			.contains("status != null and !status")
			.doesNotContain("test=\"status != null\">");
	}

}
