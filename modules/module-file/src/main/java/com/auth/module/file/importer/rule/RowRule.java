package com.auth.module.file.importer.rule;

import com.auth.module.file.importer.model.ImportRowError;

import java.util.List;

/**
 * 单行校验规则：无状态，返回 0..n 条错误。
 *
 * @param <P> 已解析行类型
 * @param <C> 导入上下文类型
 * @author Bunny
 */
@FunctionalInterface
public interface RowRule<P, C> {

	/**
	 * 执行校验
	 * @param parsed 已解析行
	 * @param rowNum 行号（1-based，不含表头）
	 * @param context 导入上下文
	 * @return 错误列表，无错误时返回空列表
	 */
	List<ImportRowError> check(P parsed, int rowNum, C context);

}
