package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.rule.RowRule;
import com.auth.module.file.importer.rule.RowRules;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 部门导入 Validate 阶段
 *
 * @author Bunny
 */
@UtilityClass
public class DeptRowValidator {

	private static final String FIELD_DEPT_CODE = "deptCode";

	private static final String FIELD_PARENT_DEPT_CODE = "parentDeptCode";

	private static final RowRule<DeptParsedRow, DeptSheetImporter.Context> DEPT_CODE_UNIQUE = RowRules.unique(
			FIELD_DEPT_CODE, DeptParsedRow::deptCode, DeptSheetImporter.Context::duplicateDeptCodesInFile,
			DeptSheetImporter.Context::existingDeptCodes);

	private static final RowRule<DeptParsedRow, DeptSheetImporter.Context> PARENT_EXISTS = (parsed, rowNum, ctx) -> {
		String parentDeptCode = parsed.parentDeptCode();
		if (parentDeptCode == null) {
			return List.of();
		}
		if (ctx.parentIdByCode().containsKey(parentDeptCode)) {
			return List.of();
		}
		return List.of(ImportErrors.referenceNotFound(rowNum, FIELD_PARENT_DEPT_CODE, parentDeptCode));
	};

	private static final List<RowRule<DeptParsedRow, DeptSheetImporter.Context>> RULES = List.of(DEPT_CODE_UNIQUE,
			PARENT_EXISTS);

	/**
	 * 校验已解析行
	 * @param parsed 已解析行
	 * @param rowNum 行号
	 * @param context 导入上下文
	 * @return 错误列表
	 */
	public static List<ImportRowError> validate(DeptParsedRow parsed, int rowNum, DeptSheetImporter.Context context) {
		return RowRules.applyFailFast(RULES, parsed, rowNum, context);
	}

}
