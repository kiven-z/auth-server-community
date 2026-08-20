package com.auth.service.system.admin.mapper.admin.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：用户业务键批量查询须一次 OR 命中四类字段
 */
@DisplayName("SysUserMapper selectRowsByBusinessKeys SQL 契约")
class SysUserMapperBusinessKeySqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = SysUserMapperBusinessKeySqlContractTest.class
			.getResourceAsStream("/mapper/admin/user/SysUserMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/admin/user/SysUserMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("业务键批量查询：四类 IN 以 OR 组合并支持 excludeUserId")
	void selectRowsByBusinessKeysUsesOrForeachForFourFields() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectRowsByBusinessKeys\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectRowsByBusinessKeys")
			.contains("u.username")
			.contains("u.email")
			.contains("u.phone")
			.contains("u.employee_no")
			.contains("excludeUserId")
			.contains("foreach");
	}

}
