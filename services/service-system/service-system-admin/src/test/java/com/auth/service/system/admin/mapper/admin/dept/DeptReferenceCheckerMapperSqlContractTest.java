package com.auth.service.system.admin.mapper.admin.dept;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：子树引用校验须覆盖用户-部门与岗位
 */
@DisplayName("SysDeptMapper existsSubtreeReference SQL 契约")
class DeptReferenceCheckerMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = DeptReferenceCheckerMapperSqlContractTest.class
			.getResourceAsStream("/mapper/admin/dept/SysDeptMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/dept/SysDeptMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("子树引用：一次查询覆盖 user_dept、sys_post")
	void existsSubtreeReferenceCoversAllReferenceSources() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"existsSubtreeReference\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("existsSubtreeReference")
			.contains("dept_closure")
			.contains("user_dept")
			.contains("sys_post")
			.contains("EXISTS")
			.doesNotContain("grant_table")
			.doesNotContain("v_user_dept_effective");
	}

}
