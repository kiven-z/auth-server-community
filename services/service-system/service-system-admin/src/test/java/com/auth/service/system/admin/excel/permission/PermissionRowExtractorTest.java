package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.importer.model.RowOutcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PermissionRowExtractor} 单元测试
 *
 * @author Bunny
 */
@DisplayName("PermissionRowExtractor 权限导入提取")
class PermissionRowExtractorTest {

	@Test
	@DisplayName("缺少 permissionCode 时返回必填错误")
	void extract_whenPermissionCodeMissing_returnsRequiredError() {
		SysPermissionImportRow row = new SysPermissionImportRow();
		row.setPermissionName("部门查询");

		RowOutcome<PermissionParsedRow> outcome = PermissionRowExtractor.extract(row, 1);

		assertThat(outcome.ok()).isFalse();
		assertThat(outcome.errors()).hasSize(1);
		assertThat(outcome.errors().get(0).message()).contains("permissionCode不能为空");
	}

	@Test
	@DisplayName("合法行提取成功并返回 trim 后字段")
	void extract_whenRowValid_returnsParsedRow() {
		SysPermissionImportRow row = new SysPermissionImportRow();
		row.setPermissionCode(" sys:dept:query ");
		row.setPermissionName(" 部门查询 ");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("备注");

		RowOutcome<PermissionParsedRow> outcome = PermissionRowExtractor.extract(row, 2);

		assertThat(outcome.ok()).isTrue();
		assertThat(outcome.value()).isNotNull();
		assertThat(outcome.value().permissionCode()).isEqualTo("sys:dept:query");
		assertThat(outcome.value().permissionName()).isEqualTo("部门查询");
		assertThat(outcome.value().statusLabel()).isEqualTo("启用");
		assertThat(outcome.value().orderNum()).isEqualTo(1);
		assertThat(outcome.value().remark()).isEqualTo("备注");
	}

}
