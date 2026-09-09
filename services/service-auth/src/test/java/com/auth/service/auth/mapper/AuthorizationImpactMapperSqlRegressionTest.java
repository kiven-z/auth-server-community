package com.auth.service.auth.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AuthorizationImpactMapper.xml SQL 语义回归测试。
 *
 * @author Bunny
 */
@DisplayName("AuthorizationImpactMapper SQL 回归")
class AuthorizationImpactMapperSqlRegressionTest {

	@Test
	@DisplayName("GRANT DEPT/POST 反查：grant_table 行删除后仍须命中成员")
	void grantSubjectReverseLookup_shouldNotDependOnGrantTableRows() throws IOException {
		// 防回归：replaceOrgSubjectRoles 先删 grant_table 再提交失效，反查不得 JOIN grant_table
		InputStream in = AuthorizationImpactMapperSqlRegressionTest.class.getClassLoader()
			.getResourceAsStream("mapper/AuthorizationImpactMapper.xml");
		assertNotNull(in, "AuthorizationImpactMapper.xml must exist in test classpath");
		String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);

		int deptFragmentStart = xml.indexOf("<sql id=\"grantTableDeptToUserIds\"");
		assertFalse(deptFragmentStart < 0, "grantTableDeptToUserIds fragment must exist");
		int deptFragmentEnd = xml.indexOf("</sql>", deptFragmentStart);
		String deptFragment = xml.substring(deptFragmentStart, deptFragmentEnd);
		assertFalse(deptFragment.contains("grant_table"),
				"DEPT grant subject fragment must not join grant_table after rows are deleted");

		int postFragmentStart = xml.indexOf("<sql id=\"grantTablePostToUserIds\"");
		assertFalse(postFragmentStart < 0, "grantTablePostToUserIds fragment must exist");
		int postFragmentEnd = xml.indexOf("</sql>", postFragmentStart);
		String postFragment = xml.substring(postFragmentStart, postFragmentEnd);
		assertFalse(postFragment.contains("grant_table"),
				"POST grant subject fragment must not join grant_table after rows are deleted");

		int deptStart = xml.indexOf("<select id=\"selectUserIdsByGrantDeptSubjectIds\"");
		assertFalse(deptStart < 0, "selectUserIdsByGrantDeptSubjectIds must exist");
		int deptEnd = xml.indexOf("</select>", deptStart);
		String deptBlock = xml.substring(deptStart, deptEnd);
		assertFalse(deptBlock.contains("gt.subject_id"),
				"DEPT grant subject lookup must filter by dept ancestor_id, not grant_table subject_id");

		int postStart = xml.indexOf("<select id=\"selectUserIdsByGrantPostSubjectIds\"");
		assertFalse(postStart < 0, "selectUserIdsByGrantPostSubjectIds must exist");
		int postEnd = xml.indexOf("</select>", postStart);
		String postBlock = xml.substring(postStart, postEnd);
		assertFalse(postBlock.contains("gt.subject_id"),
				"POST grant subject lookup must filter by post_id, not grant_table subject_id");
	}

	@Test
	@DisplayName("权限码桥接：id 关联后仍不得按 sys_permission.status 过滤")
	void permissionBridge_shouldNotFilterBySysPermissionStatus() throws IOException {
		// 防回归：权限停用/改码时 impacted 不能因为 status 过滤而变为 0
		InputStream in = AuthorizationImpactMapperSqlRegressionTest.class.getClassLoader()
			.getResourceAsStream("mapper/AuthorizationImpactMapper.xml");
		assertNotNull(in, "AuthorizationImpactMapper.xml must exist in test classpath");
		byte[] bytes = in.readAllBytes();
		String xml = new String(bytes, StandardCharsets.UTF_8);

		int selectStart = xml.indexOf("<select id=\"selectRoleCodesByPermissionCodes\"");
		assertNotNull(xml);
		assertFalse(selectStart < 0, "selectRoleCodesByPermissionCodes must exist");
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertFalse(selectBlock.contains("AND p.status = 1"),
				"Permission bridge should not filter by sys_permission.status (breaks on disable)");
		assertFalse(selectBlock.contains("p.status ="),
				"Permission bridge should not filter by sys_permission.status (breaks on disable)");
	}

}
