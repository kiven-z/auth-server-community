package com.auth.service.auth.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约测试：防止授权失效「影响面反查」出现不符合预期的过滤条件。
 */
@DisplayName("AuthorizationImpactMapper SQL 契约")
class AuthorizationImpactMapperSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = AuthorizationImpactMapperSqlContractTest.class
			.getResourceAsStream("/mapper/AuthorizationImpactMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/AuthorizationImpactMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("权限码桥接：经 permission_id/role_id 关联，但不按权限启用状态过滤")
	void permissionCodeBridgeShouldJoinByIdWithoutStatusFilter() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectRoleCodesByPermissionCodes\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		// grant 表已改为 id 关联，须 join sys_permission / sys_role 才能按 permission_code 桥接
		assertThat(selectBlock).contains("selectRoleCodesByPermissionCodes")
			.contains("sys_role_permission rp")
			.contains("INNER JOIN sys_permission p ON p.id = rp.permission_id")
			.contains("INNER JOIN sys_role r ON r.id = rp.role_id")
			.contains("p.permission_code IN")
			.doesNotContain("p.status")
			.doesNotContain(" p.status ");
	}

}
