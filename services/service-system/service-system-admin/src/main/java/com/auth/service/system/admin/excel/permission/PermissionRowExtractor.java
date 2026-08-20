package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.importer.model.RowOutcome;
import com.auth.module.file.importer.parse.ImportTextParsers;
import lombok.experimental.UtilityClass;

/**
 * 权限导入 Extract 阶段：trim、必填与字段提取
 *
 * @author Bunny
 */
@UtilityClass
public class PermissionRowExtractor {

	private static final String FIELD_PERMISSION_CODE = "permissionCode";

	private static final String FIELD_PERMISSION_NAME = "permissionName";

	/**
	 * 从 Excel 行提取并解析字段
	 * @param row Excel 行
	 * @param rowNum 行号
	 * @return 解析结果
	 */
	public static RowOutcome<PermissionParsedRow> extract(SysPermissionImportRow row, int rowNum) {
		RowOutcome<String> permissionCode = ImportTextParsers.require(row.getPermissionCode(), rowNum,
				FIELD_PERMISSION_CODE);
		if (!permissionCode.ok()) {
			return RowOutcome.err(permissionCode.errors());
		}
		RowOutcome<String> permissionName = ImportTextParsers.require(row.getPermissionName(), rowNum,
				FIELD_PERMISSION_NAME);
		if (!permissionName.ok()) {
			return RowOutcome.err(permissionName.errors());
		}

		return RowOutcome.ok(PermissionParsedRow.builder()
			.permissionCode(permissionCode.value())
			.permissionName(permissionName.value())
			.statusLabel(row.getStatusLabel())
			.orderNum(row.getOrderNum())
			.remark(row.getRemark())
			.build());
	}

}
