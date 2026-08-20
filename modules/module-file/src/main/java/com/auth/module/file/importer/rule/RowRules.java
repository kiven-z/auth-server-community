package com.auth.module.file.importer.rule;

import com.auth.module.file.importer.model.ImportErrorCode;
import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.ImportRowError;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * {@link RowRule} 组合与常用规则工厂。
 *
 * @author Bunny
 */
@UtilityClass
public class RowRules {

	/**
	 * 按顺序执行规则，遇到首个错误即停止（fail-fast）
	 * @param rules 规则列表
	 * @param parsed 已解析行
	 * @param rowNum 行号
	 * @param context 导入上下文
	 * @param <P> 已解析行类型
	 * @param <C> 导入上下文类型
	 * @return 错误列表
	 */
	public static <P, C> List<ImportRowError> applyFailFast(List<RowRule<P, C>> rules, P parsed, int rowNum,
			C context) {
		for (RowRule<P, C> rule : rules) {
			List<ImportRowError> found = rule.check(parsed, rowNum, context);
			if (!found.isEmpty()) {
				return List.of(found.get(0));
			}
		}
		return List.of();
	}

	/**
	 * 按顺序执行规则，收集全部错误
	 * @param rules 规则列表
	 * @param parsed 已解析行
	 * @param rowNum 行号
	 * @param context 导入上下文
	 * @param <P> 已解析行类型
	 * @param <C> 导入上下文类型
	 * @return 错误列表
	 */
	public static <P, C> List<ImportRowError> applyAll(List<RowRule<P, C>> rules, P parsed, int rowNum, C context) {
		List<ImportRowError> errors = new ArrayList<>();
		for (RowRule<P, C> rule : rules) {
			errors.addAll(rule.check(parsed, rowNum, context));
		}
		return errors;
	}

	/**
	 * 文件内唯一性与库中不存在性
	 * @param field 字段名
	 * @param getter 字段值提取
	 * @param fileDups 文件内重复值
	 * @param dbExists 库中已存在值
	 * @param <P> 已解析行类型
	 * @param <C> 导入上下文类型
	 * @return 唯一性规则
	 */
	public static <P, C> RowRule<P, C> unique(String field, Function<P, String> getter,
			Function<C, Set<String>> fileDups, Function<C, Set<String>> dbExists) {
		return (parsed, rowNum, ctx) -> {
			String value = getter.apply(parsed);
			if (value == null) {
				return List.of();
			}
			if (fileDups.apply(ctx).contains(value)) {
				return List.of(ImportErrors.duplicateInFile(rowNum, field, value));
			}
			if (dbExists.apply(ctx).contains(value)) {
				return List.of(ImportErrors.alreadyExists(rowNum, field, value));
			}
			return List.of();
		};
	}

	/**
	 * 字段长度范围
	 * @param field 字段名
	 * @param getter 字段值提取
	 * @param min 最小长度
	 * @param max 最大长度
	 * @param <P> 已解析行类型
	 * @return 长度规则
	 */
	public static <P, C> RowRule<P, C> lengthBetween(String field, Function<P, String> getter, int min, int max) {
		return (parsed, rowNum, ctx) -> {
			String value = getter.apply(parsed);
			if (value == null) {
				return List.of();
			}
			if (value.length() < min || value.length() > max) {
				return List.of(ImportErrors.lengthOutOfRange(rowNum, field, min, max));
			}
			return List.of();
		};
	}

	/**
	 * 引用目标存在
	 * @param field 字段名
	 * @param refGetter 引用目标提取
	 * @param rawGetter 原始值提取
	 * @param <P> 已解析行类型
	 * @param <C> 导入上下文类型
	 * @return 引用存在规则
	 */
	public static <P, C> RowRule<P, C> refExists(String field, Function<P, Object> refGetter,
			Function<P, String> rawGetter) {
		return (parsed, rowNum, ctx) -> {
			if (refGetter.apply(parsed) == null) {
				return List.of(ImportErrors.referenceNotFound(rowNum, field, rawGetter.apply(parsed)));
			}
			return List.of();
		};
	}

	/**
	 * 条件断言
	 * @param field 字段名
	 * @param valueGetter 值提取
	 * @param condition 条件
	 * @param code 错误类型
	 * @param <P> 已解析行类型
	 * @return 断言规则
	 */
	public static <P, C> RowRule<P, C> assertThat(String field, Function<P, String> valueGetter, Predicate<P> condition,
			ImportErrorCode code) {
		return (parsed, rowNum, ctx) -> {
			if (!condition.test(parsed)) {
				return List.of(ImportErrors.of(rowNum, code, field, valueGetter.apply(parsed)));
			}
			return List.of();
		};
	}

}
