package com.auth.service.system.admin.mapper.admin.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位可挂载部门打 sys_dept
 */
@DisplayName("SysPostMapper selectAssignableDeptIds SQL 契约")
class SysPostMapperAssignableDeptSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = SysPostMapperAssignableDeptSqlContractTest.class
			.getResourceAsStream("/mapper/admin/post/SysPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/post/SysPostMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("可挂载部门：打 sys_dept，不按启用状态过滤")
	void selectAssignableDeptIdsUsesBaseTable() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectAssignableDeptIds\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectAssignableDeptIds")
			.contains("sys_dept d")
			.contains("foreach")
			.doesNotContain("v_dept_effective")
			.doesNotContain("v_post_effective")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("d.status = 1");
	}

}
