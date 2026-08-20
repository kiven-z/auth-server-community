package com.auth.module.file.importer.model;

import java.util.List;

/**
 * Excel 批量导入结果
 *
 * @param success 是否全部成功（无任何行错误）
 * @param importedCount 成功导入的行数
 * @param errors 失败行列表；成功时为空列表
 * @author Bunny
 */
public record SpreadsheetImportResult(boolean success, int importedCount, List<ImportRowError> errors) {

	/**
	 * 全部成功
	 * @param importedCount 导入行数
	 * @return 成功结果
	 */
	public static SpreadsheetImportResult success(int importedCount) {
		return new SpreadsheetImportResult(true, importedCount, List.of());
	}

	/**
	 * 存在行级错误，未写入任何数据
	 * @param errors 失败行列表
	 * @return 失败结果
	 */
	public static SpreadsheetImportResult withErrors(List<ImportRowError> errors) {
		return new SpreadsheetImportResult(false, 0, errors);
	}

}
