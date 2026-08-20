package com.auth.service.system.admin.excel.role;

import com.auth.module.file.importer.model.RowOutcome;
import com.auth.module.file.importer.parse.ImportTextParsers;
import lombok.experimental.UtilityClass;

/**
 * 角色导入 Extract 阶段：trim、必填与字段提取
 *
 * @author Bunny
 */
@UtilityClass
public class RoleRowExtractor {

	private static final String FIELD_ROLE_CODE = "roleCode";

	private static final String FIELD_ROLE_NAME = "roleName";

	/**
	 * 从 Excel 行提取并解析字段
	 * @param row Excel 行
	 * @param rowNum 行号
	 * @return 解析结果
	 */
	public static RowOutcome<RoleParsedRow> extract(SysRoleImportRow row, int rowNum) {
		RowOutcome<String> roleCode = ImportTextParsers.require(row.getRoleCode(), rowNum, FIELD_ROLE_CODE);
		if (!roleCode.ok()) {
			return RowOutcome.err(roleCode.errors());
		}
		RowOutcome<String> roleName = ImportTextParsers.require(row.getRoleName(), rowNum, FIELD_ROLE_NAME);
		if (!roleName.ok()) {
			return RowOutcome.err(roleName.errors());
		}

		return RowOutcome.ok(RoleParsedRow.builder()
			.roleCode(roleCode.value())
			.roleName(roleName.value())
			.statusLabel(row.getStatusLabel())
			.orderNum(row.getOrderNum())
			.remark(row.getRemark())
			.build());
	}

}
