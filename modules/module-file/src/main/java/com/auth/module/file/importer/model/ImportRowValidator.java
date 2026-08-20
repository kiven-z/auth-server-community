package com.auth.module.file.importer.model;

import com.auth.module.file.importer.parse.ImportTextParsers;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 单行导入校验流水线（fail-fast）；内部以 {@link RowOutcome} 表达行状态
 *
 * @author Bunny
 */
public final class ImportRowValidator {

	private final int rowNum;

	private final List<ImportRowError> errors;

	private RowOutcome<Void> outcome;

	private ImportRowValidator(int rowNum, List<ImportRowError> errors) {
		this.rowNum = rowNum;
		this.errors = errors;
		this.outcome = RowOutcome.ok(null);
	}

	/**
	 * 创建当前行的校验流水线
	 * @param rowNum 行号（1-based，不含表头）
	 * @param errors 错误收集列表
	 * @return 校验器实例
	 */
	public static ImportRowValidator of(int rowNum, List<ImportRowError> errors) {
		return new ImportRowValidator(rowNum, errors);
	}

	/**
	 * 当前行号
	 * @return 行号
	 */
	public int rowNum() {
		return rowNum;
	}

	/**
	 * 是否尚未失败
	 * @return 是否尚未失败
	 */
	public boolean ok() {
		return outcome.ok();
	}

	/**
	 * 是否已失败
	 * @return 是否已失败
	 */
	public boolean failed() {
		return !outcome.ok();
	}

	/**
	 * 校验必填文本并返回 trim 后的值
	 * @param raw 原始值
	 * @param field 字段名
	 * @return trim 后的值
	 */
	public String require(String raw, String field) {
		RowOutcome<String> parsed = requireOutcome(raw, field);
		if (!parsed.ok()) {
			recordFirstError(parsed);
			return null;
		}
		return parsed.value();
	}

	/**
	 * 校验必填文本，返回 {@link RowOutcome}
	 * @param raw 原始值
	 * @param field 字段名
	 * @return 解析结果
	 */
	public RowOutcome<String> requireOutcome(String raw, String field) {
		if (!outcome.ok()) {
			return RowOutcome.err(outcome.errors());
		}
		return ImportTextParsers.require(raw, rowNum, field);
	}

	/**
	 * 校验文件内唯一性与库中不存在性
	 * @param value 值
	 * @param fileDups 文件内重复值集合
	 * @param dbExists 库中已存在值集合
	 * @param field 字段名
	 * @return 校验器实例
	 */
	public ImportRowValidator unique(String value, Set<String> fileDups, Set<String> dbExists, String field) {
		if (!outcome.ok() || value == null) {
			return this;
		}
		if (fileDups.contains(value)) {
			fail(ImportErrors.duplicateInFile(rowNum, field, value));
		}
		else if (dbExists.contains(value)) {
			fail(ImportErrors.alreadyExists(rowNum, field, value));
		}
		return this;
	}

	/**
	 * 断言条件成立，否则按错误类型记录失败
	 * @param condition 条件
	 * @param code 错误类型
	 * @param args 类型相关参数
	 * @return 校验器实例
	 */
	public ImportRowValidator assertThat(boolean condition, ImportErrorCode code, Object... args) {
		if (!outcome.ok() || condition) {
			return this;
		}
		fail(ImportErrors.of(rowNum, code, args));
		return this;
	}

	/**
	 * 校验字段长度在范围内
	 * @param value 值
	 * @param min 最小长度
	 * @param max 最大长度
	 * @param field 字段名
	 * @return 校验器实例
	 */
	public ImportRowValidator lengthBetween(String value, int min, int max, String field) {
		if (!outcome.ok() || value == null) {
			return this;
		}
		if (value.length() < min || value.length() > max) {
			fail(ImportErrors.lengthOutOfRange(rowNum, field, min, max));
		}
		return this;
	}

	/**
	 * 校验引用目标存在
	 * @param ref 引用目标
	 * @param field 字段名
	 * @param rawValue 原始值
	 * @return 校验器实例
	 */
	public ImportRowValidator refExists(Object ref, String field, String rawValue) {
		if (!outcome.ok()) {
			return this;
		}
		if (ref == null) {
			fail(ImportErrors.referenceNotFound(rowNum, field, rawValue));
		}
		return this;
	}

	/**
	 * 记录预构建的行级错误
	 * @param error 行级错误
	 * @return 校验器实例
	 */
	public ImportRowValidator fail(ImportRowError error) {
		if (outcome.ok()) {
			errors.add(error);
			outcome = RowOutcome.err(error);
		}
		return this;
	}

	/**
	 * 合并 {@link RowOutcome} 错误到当前校验器（fail-fast，仅记录首个错误）
	 * @param result 待合并结果
	 * @return 校验器实例
	 */
	public ImportRowValidator merge(RowOutcome<?> result) {
		if (outcome.ok() && !result.ok()) {
			recordFirstError(result);
		}
		return this;
	}

	/**
	 * 尚未失败时执行后续步骤
	 * @param step 后续步骤
	 * @return 校验器实例
	 */
	public ImportRowValidator whenOk(Consumer<ImportRowValidator> step) {
		if (outcome.ok()) {
			step.accept(this);
		}
		return this;
	}

	/**
	 * 校验通过时构建结果；已失败时返回 null，不再执行 builder
	 * @param builder 构建器
	 * @param <F> 构建结果类型
	 * @return 构建结果
	 */
	public <F> F mapOrNull(Supplier<F> builder) {
		if (!outcome.ok()) {
			return null;
		}
		return builder.get();
	}

	private void recordFirstError(RowOutcome<?> result) {
		List<ImportRowError> resultErrors = result.errors();
		if (!resultErrors.isEmpty()) {
			fail(resultErrors.get(0));
		}
	}

}
