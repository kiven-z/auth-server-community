package com.auth.service.system.admin.mapper.admin.dept;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeptClosureMapper 删除 SQL 契约测试
 *
 * @author Bunny
 */
@DisplayName("DeptClosureMapper deletePaths SQL 契约")
class DeptClosureMapperDeleteSqlContractTest {

	private static String readMapperXml() throws IOException {
		try (InputStream in = DeptClosureMapperDeleteSqlContractTest.class
			.getResourceAsStream("/mapper/admin/dept/DeptClosureMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/dept/DeptClosureMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("删除子树闭包：使用派生子树删除，不调用存储过程")
	void deletePathsUsesDerivedTableDelete() throws IOException {
		// 读取 Mapper，截取 deletePaths 片段做关键 SQL 契约断言。
		String xml = readMapperXml();
		int updateStart = xml.indexOf("<update id=\"deletePaths\"");
		assertThat(updateStart).isNotNegative();
		int updateEnd = xml.indexOf("</update>", updateStart);
		assertThat(updateEnd).isGreaterThan(updateStart);
		String updateBlock = xml.substring(updateStart, updateEnd);

		assertThat(updateBlock).contains("DELETE dc")
			.contains("INNER JOIN dept_closure sub ON sub.descendant_id = dc.descendant_id")
			.contains("sub.ancestor_id = #{deptId}")
			.doesNotContain("statementType=\"CALLABLE\"")
			.doesNotContain("sp_dept_delete")
			.doesNotContain("is_deleted");
	}

}
