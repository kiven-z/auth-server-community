package com.auth.service.system.admin.excel.role;

import com.auth.module.file.importer.model.RowOutcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RoleRowExtractor} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RoleRowExtractor 角色导入提取")
class RoleRowExtractorTest {

	@Test
	@DisplayName("缺少 roleCode 时返回必填错误")
	void extract_whenRoleCodeMissing_returnsRequiredError() {
		SysRoleImportRow row = new SysRoleImportRow();
		row.setRoleName("管理员");

		RowOutcome<RoleParsedRow> outcome = RoleRowExtractor.extract(row, 1);

		assertThat(outcome.ok()).isFalse();
		assertThat(outcome.errors()).hasSize(1);
		assertThat(outcome.errors().get(0).message()).contains("roleCode不能为空");
	}

	@Test
	@DisplayName("合法行提取成功并返回 trim 后字段")
	void extract_whenRowValid_returnsParsedRow() {
		SysRoleImportRow row = new SysRoleImportRow();
		row.setRoleCode(" ADMIN ");
		row.setRoleName(" 管理员 ");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("备注");

		RowOutcome<RoleParsedRow> outcome = RoleRowExtractor.extract(row, 2);

		assertThat(outcome.ok()).isTrue();
		assertThat(outcome.value()).isNotNull();
		assertThat(outcome.value().roleCode()).isEqualTo("ADMIN");
		assertThat(outcome.value().roleName()).isEqualTo("管理员");
		assertThat(outcome.value().statusLabel()).isEqualTo("启用");
		assertThat(outcome.value().orderNum()).isEqualTo(1);
		assertThat(outcome.value().remark()).isEqualTo("备注");
	}

}
