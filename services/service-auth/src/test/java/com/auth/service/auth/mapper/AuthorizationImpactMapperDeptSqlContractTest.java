package com.auth.service.auth.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 契约：部门失效反查须覆盖禁用节点（不按 status 过滤），与正向画像重算规则分离。
 */
@DisplayName("AuthorizationImpactMapper 部门反查 SQL 契约")
class AuthorizationImpactMapperDeptSqlContractTest {

	private static String readClasspathText() throws IOException {
		try (InputStream in = AuthorizationImpactMapperDeptSqlContractTest.class
			.getResourceAsStream("/mapper/AuthorizationImpactMapper.xml")) {
			assertThat(in).as("resource %s must exist", "/mapper/AuthorizationImpactMapper.xml").isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Test
	@DisplayName("按部门 ID 反查成员：不按 sys_dept.status 过滤，以便禁用/移动仍能刷新画像")
	void selectUserIdsByDeptIdsDoesNotFilterByDeptStatus() throws IOException {
		String xml = readClasspathText();

		int selectStart = xml.indexOf("<select id=\"selectUserIdsByDeptIds\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(selectBlock).contains("selectUserIdsByDeptIds")
			.doesNotContain("is_deleted")
			.doesNotContain("d.status =")
			.doesNotContain("v_user_dept_effective")
			.doesNotContain("ud.status");
	}

	@Test
	@DisplayName("GRANT DEPT 主体反查：经 user_dept + dept_closure 展开，不依赖 grant_table 现存行")
	void selectUserIdsByGrantDeptSubjectIdsShouldExpandDeptWithoutGrantTable() throws IOException {
		String xml = readClasspathText();

		int fragmentStart = xml.indexOf("<sql id=\"grantTableDeptToUserIds\"");
		assertThat(fragmentStart).isNotNegative();
		int fragmentEnd = xml.indexOf("</sql>", fragmentStart);
		String fragmentBlock = xml.substring(fragmentStart, fragmentEnd);

		int selectStart = xml.indexOf("<select id=\"selectUserIdsByGrantDeptSubjectIds\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		// grant_table 行可能已删除，反查须与 selectUserIdsByDeptIds 同源展开
		assertThat(fragmentBlock).contains("user_dept ud")
			.contains("dept_closure dc")
			.doesNotContain("is_deleted")
			.doesNotContain("grant_table")
			.doesNotContain("d.status =");
		assertThat(selectBlock).contains("selectUserIdsByGrantDeptSubjectIds")
			.contains("dc.ancestor_id IN")
			.doesNotContain("ud.status")
			.doesNotContain("gt.subject_id");
	}

	@Test
	@DisplayName("GRANT POST 主体反查：经 user_post 定位成员，不依赖 grant_table 现存行")
	void selectUserIdsByGrantPostSubjectIdsShouldResolvePostWithoutGrantTable() throws IOException {
		String xml = readClasspathText();

		int fragmentStart = xml.indexOf("<sql id=\"grantTablePostToUserIds\"");
		assertThat(fragmentStart).isNotNegative();
		int fragmentEnd = xml.indexOf("</sql>", fragmentStart);
		String fragmentBlock = xml.substring(fragmentStart, fragmentEnd);

		int selectStart = xml.indexOf("<select id=\"selectUserIdsByGrantPostSubjectIds\"");
		assertThat(selectStart).isNotNegative();
		int selectEnd = xml.indexOf("</select>", selectStart);
		String selectBlock = xml.substring(selectStart, selectEnd);

		assertThat(fragmentBlock).contains("user_post up").doesNotContain("grant_table").doesNotContain("is_deleted");
		assertThat(selectBlock).contains("selectUserIdsByGrantPostSubjectIds")
			.contains("up.post_id IN")
			.doesNotContain("v_user_post_effective")
			.doesNotContain("up.status")
			.doesNotContain("gt.subject_id");
	}

}
