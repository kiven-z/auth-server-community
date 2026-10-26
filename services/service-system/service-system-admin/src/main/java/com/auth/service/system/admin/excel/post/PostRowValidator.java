package com.auth.service.system.admin.excel.post;

import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.rule.RowRule;
import com.auth.module.file.importer.rule.RowRules;
import com.auth.service.system.admin.support.post.PostWriteGuard;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 岗位导入 Validate 阶段。
 *
 * @author Bunny
 */
@UtilityClass
public class PostRowValidator {

	private static final String FIELD_DEPT_CODE = "deptCode";

	/**
	 * 文件内重复校验规则
	 */
	private static final RowRule<PostParsedRow, PostSheetImporter.Context> FILE_DUPLICATE = (parsed, rowNum, ctx) -> {
		String fileCompositeKey = PostWriteGuard.deptCodePostCodeKey(parsed.deptCode(), parsed.postCode());
		if (ctx.duplicateCompositeKeysInFile().contains(fileCompositeKey)) {
			return List.of(ImportErrors.duplicatePostInFile(rowNum, parsed.postCode(), parsed.deptCode()));
		}
		return List.of();
	};

	/**
	 * 部门存在校验规则
	 */
	private static final RowRule<PostParsedRow, PostSheetImporter.Context> DEPT_EXISTS = (parsed, rowNum, ctx) -> {
		Long deptId = ctx.deptIdByCode().get(parsed.deptCode());
		if (deptId == null) {
			return List.of(ImportErrors.referenceNotFound(rowNum, FIELD_DEPT_CODE, parsed.deptCode()));
		}
		return List.of();
	};

	/**
	 * 数据库存在校验规则
	 */
	private static final RowRule<PostParsedRow, PostSheetImporter.Context> DB_EXISTS = (parsed, rowNum, ctx) -> {
		Long deptId = ctx.deptIdByCode().get(parsed.deptCode());
		if (deptId == null) {
			return List.of();
		}
		String dbCompositeKey = PostWriteGuard.deptIdPostCodeKey(deptId, parsed.postCode());
		if (ctx.existingCompositeKeys().contains(dbCompositeKey)) {
			return List.of(ImportErrors.alreadyExistsPost(rowNum, parsed.postCode(), parsed.deptCode()));
		}
		return List.of();
	};

	/**
	 * 校验已解析行
	 * @param parsed 已解析行
	 * @param rowNum 行号
	 * @param context 导入上下文
	 * @return 错误列表
	 */
	public static List<ImportRowError> validate(PostParsedRow parsed, int rowNum, PostSheetImporter.Context context) {
		// 岗位导入校验规则
		List<RowRule<PostParsedRow, PostSheetImporter.Context>> rules = List.of(FILE_DUPLICATE, DEPT_EXISTS, DB_EXISTS);
		return RowRules.applyFailFast(rules, parsed, rowNum, context);
	}

}
