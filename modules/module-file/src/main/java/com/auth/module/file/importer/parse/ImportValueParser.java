package com.auth.module.file.importer.parse;

import com.auth.module.file.importer.model.RowOutcome;

/**
 * 导入字段值解析器（枚举、日期等）。
 *
 * @param <T> 解析结果类型
 * @author Bunny
 */
@FunctionalInterface
public interface ImportValueParser<T> {

	/**
	 * 解析原始文本
	 * @param raw 原始值
	 * @param rowNum 行号（1-based，不含表头）
	 * @param field 字段名
	 * @return 解析结果
	 */
	RowOutcome<T> parse(String raw, int rowNum, String field);

}
