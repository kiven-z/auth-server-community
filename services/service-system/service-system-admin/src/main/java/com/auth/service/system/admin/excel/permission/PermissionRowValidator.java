package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.importer.model.ImportErrorCode;
import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.rule.RowRule;
import com.auth.module.file.importer.rule.RowRules;
import com.auth.module.security.contract.convention.AuthCodeConvention;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 权限导入 Validate 阶段
 *
 * @author Bunny
 */
@UtilityClass
public class PermissionRowValidator {

	private static final String FIELD_PERMISSION_CODE = "permissionCode";

	private static final String FIELD_STATUS = "status";

	private static final RowRule<PermissionParsedRow, PermissionSheetImporter.Context> PERMISSION_CODE_UNIQUE = RowRules
		.unique(FIELD_PERMISSION_CODE, PermissionParsedRow::permissionCode,
				PermissionSheetImporter.Context::duplicatePermissionCodesInFile,
				PermissionSheetImporter.Context::existingPermissionCodes);

	private static final RowRule<PermissionParsedRow, PermissionSheetImporter.Context> PERMISSION_CODE_FORMAT = RowRules
		.assertThat(FIELD_PERMISSION_CODE, PermissionParsedRow::permissionCode,
				parsed -> AuthCodeConvention.isWellFormedPermissionCode(parsed.permissionCode()),
				ImportErrorCode.INVALID_FORMAT);

	private static final RowRule<PermissionParsedRow, PermissionSheetImporter.Context> ENABLE_STATUS = (parsed, rowNum,
			ctx) -> {
		if (EnableStatusLabels.parseImport(parsed.statusLabel()) == EnableStatus.UNKNOWN) {
			return List.of(ImportErrors.invalidValue(rowNum, FIELD_STATUS, parsed.statusLabel()));
		}
		return List.of();
	};

	private static final List<RowRule<PermissionParsedRow, PermissionSheetImporter.Context>> RULES = List
		.of(PERMISSION_CODE_UNIQUE, PERMISSION_CODE_FORMAT, ENABLE_STATUS);

	/**
	 * 校验已解析行
	 * @param parsed 已解析行
	 * @param rowNum 行号
	 * @param context 导入上下文
	 * @return 错误列表
	 */
	public static List<ImportRowError> validate(PermissionParsedRow parsed, int rowNum,
			PermissionSheetImporter.Context context) {
		return RowRules.applyFailFast(RULES, parsed, rowNum, context);
	}

}
