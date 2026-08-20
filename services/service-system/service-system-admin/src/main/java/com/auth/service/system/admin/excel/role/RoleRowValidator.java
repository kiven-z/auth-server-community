package com.auth.service.system.admin.excel.role;

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
 * 角色导入 Validate 阶段
 *
 * @author Bunny
 */
@UtilityClass
public class RoleRowValidator {

	private static final String FIELD_ROLE_CODE = "roleCode";

	private static final String FIELD_STATUS = "status";

	private static final RowRule<RoleParsedRow, RoleSheetImporter.Context> ROLE_CODE_UNIQUE = RowRules.unique(
			FIELD_ROLE_CODE, RoleParsedRow::roleCode, RoleSheetImporter.Context::duplicateRoleCodesInFile,
			RoleSheetImporter.Context::existingRoleCodes);

	private static final RowRule<RoleParsedRow, RoleSheetImporter.Context> ROLE_CODE_FORMAT = RowRules.assertThat(
			FIELD_ROLE_CODE, RoleParsedRow::roleCode,
			parsed -> AuthCodeConvention.isWellFormedRoleCode(parsed.roleCode()), ImportErrorCode.INVALID_FORMAT);

	private static final RowRule<RoleParsedRow, RoleSheetImporter.Context> ENABLE_STATUS = (parsed, rowNum, ctx) -> {
		if (EnableStatusLabels.parseImport(parsed.statusLabel()) == EnableStatus.UNKNOWN) {
			return List.of(ImportErrors.invalidValue(rowNum, FIELD_STATUS, parsed.statusLabel()));
		}
		return List.of();
	};

	private static final List<RowRule<RoleParsedRow, RoleSheetImporter.Context>> RULES = List.of(ROLE_CODE_UNIQUE,
			ROLE_CODE_FORMAT, ENABLE_STATUS);

	/**
	 * 校验已解析行
	 * @param parsed 已解析行
	 * @param rowNum 行号
	 * @param context 导入上下文
	 * @return 错误列表
	 */
	public static List<ImportRowError> validate(RoleParsedRow parsed, int rowNum, RoleSheetImporter.Context context) {
		return RowRules.applyFailFast(RULES, parsed, rowNum, context);
	}

}
