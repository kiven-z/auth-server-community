package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.importer.model.RowOutcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DeptRowExtractor} 单元测试
 *
 * @author Bunny
 */
@DisplayName("DeptRowExtractor 部门导入提取")
class DeptRowExtractorTest {

	@Test
	@DisplayName("缺少 deptCode 时返回必填错误")
	void extract_whenDeptCodeMissing_returnsRequiredError() {
		SysDeptImportRow row = new SysDeptImportRow();
		row.setDeptName("研发部");

		RowOutcome<DeptParsedRow> outcome = DeptRowExtractor.extract(row, 1);

		assertThat(outcome.ok()).isFalse();
		assertThat(outcome.errors()).hasSize(1);
		assertThat(outcome.errors().get(0).message()).contains("deptCode不能为空");
	}

	@Test
	@DisplayName("合法行提取成功并返回 trim 后字段")
	void extract_whenRowValid_returnsParsedRow() {
		SysDeptImportRow row = new SysDeptImportRow();
		row.setParentDeptCode(" ROOT ");
		row.setDeptCode(" RD ");
		row.setDeptName(" 研发部 ");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("备注");

		RowOutcome<DeptParsedRow> outcome = DeptRowExtractor.extract(row, 2);

		assertThat(outcome.ok()).isTrue();
		assertThat(outcome.value()).isNotNull();
		assertThat(outcome.value().parentDeptCode()).isEqualTo("ROOT");
		assertThat(outcome.value().deptCode()).isEqualTo("RD");
		assertThat(outcome.value().deptName()).isEqualTo("研发部");
		assertThat(outcome.value().statusLabel()).isEqualTo("启用");
		assertThat(outcome.value().orderNum()).isEqualTo(1);
		assertThat(outcome.value().remark()).isEqualTo("备注");
	}

}
