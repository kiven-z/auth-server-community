package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.importer.model.RowOutcome;
import com.auth.module.file.importer.parse.ImportTextParsers;
import lombok.experimental.UtilityClass;

/**
 * 部门导入 Extract 阶段：trim、必填与字段提取
 *
 * @author Bunny
 */
@UtilityClass
public class DeptRowExtractor {

	private static final String FIELD_DEPT_CODE = "deptCode";

	private static final String FIELD_DEPT_NAME = "deptName";

	/**
	 * 从 Excel 行提取并解析字段
	 * @param row Excel 行
	 * @param rowNum 行号
	 * @return 解析结果
	 */
	public static RowOutcome<DeptParsedRow> extract(SysDeptImportRow row, int rowNum) {
		RowOutcome<String> deptCode = ImportTextParsers.require(row.getDeptCode(), rowNum, FIELD_DEPT_CODE);
		if (!deptCode.ok()) {
			return RowOutcome.err(deptCode.errors());
		}
		RowOutcome<String> deptName = ImportTextParsers.require(row.getDeptName(), rowNum, FIELD_DEPT_NAME);
		if (!deptName.ok()) {
			return RowOutcome.err(deptName.errors());
		}

		return RowOutcome.ok(DeptParsedRow.builder()
			.parentDeptCode(ImportTextParsers.optional(row.getParentDeptCode()))
			.deptCode(deptCode.value())
			.deptName(deptName.value())
			.statusLabel(row.getStatusLabel())
			.orderNum(row.getOrderNum())
			.remark(row.getRemark())
			.build());
	}

}
