package com.auth.service.system.admin.mapper.admin.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：岗位 (dept_id, post_code) 批量存在性查询须按对精确匹配
 */
@DisplayName("SysPostMapper selectReferenceByDeptPostPairs SQL 契约")
class SysPostMapperDeptPostPairSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = SysPostMapperDeptPostPairSqlContractTest.class
			.getResourceAsStream("/mapper/admin/post/SysPostMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/post/SysPostMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("部门岗位对批量查询：OR 组合 dept_id 与 post_code 等值条件")
	void selectReferenceByDeptPostPairsMatchesCompositePairs() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectReferenceByDeptPostPairs\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectReferenceByDeptPostPairs")
			.contains("p.dept_id")
			.contains("p.post_code")
			.doesNotContain("is_deleted")
			.contains("pair.deptId")
			.contains("pair.postCode")
			.contains("foreach");
	}

}
