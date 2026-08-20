package com.auth.service.system.admin.excel;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.ImportRowValidator;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Excel 导入批量预检工具（行级校验已迁移至
 * {@link com.auth.module.file.importer.model.ImportRowValidator}）。
 *
 * @author Bunny
 */
@UtilityClass
public class ImportRowSupport {

	/**
	 * 导入校验字段名:status
	 */
	private static final String FIELD_STATUS = "status";

	/**
	 * 从行集合中提取非空字段值（trim 后去重）。
	 * @param rows 行数据
	 * @param getter 字段提取函数
	 * @param <R> 行类型
	 * @return 去重后的字段值列表
	 */
	public static <R> List<String> collectDistinct(List<R> rows, Function<R, String> getter) {
		return rows.stream().map(getter).filter(CharSequenceUtil::isNotBlank).map(String::trim).distinct().toList();
	}

	/**
	 * 检测文件内重复字段值。
	 * @param rows 行数据
	 * @param getter 字段提取函数
	 * @param <R> 行类型
	 * @return 在文件内出现多次的字段值集合
	 */
	public static <R> Set<String> duplicatesInFile(List<R> rows, Function<R, String> getter) {
		Set<String> seen = new HashSet<>();
		Set<String> duplicates = new HashSet<>();
		for (R row : rows) {
			if (CharSequenceUtil.isBlank(getter.apply(row))) {
				continue;
			}

			String value = getter.apply(row).trim();
			if (!seen.add(value)) {
				duplicates.add(value);
			}
		}
		return duplicates;
	}

	/**
	 * 批量预检编码在文件内及库中的唯一性。
	 * @param rows 行数据
	 * @param codeGetter 编码提取函数
	 * @param existingLoader 按编码列表加载库中已存在编码
	 * @param <R> 行类型
	 * @return 唯一性预检结果
	 */
	public static <R> CodeUniqueness prepareCodeUniqueness(List<R> rows, Function<R, String> codeGetter,
			Function<List<String>, Set<String>> existingLoader) {
		Set<String> duplicatesInFile = duplicatesInFile(rows, codeGetter);
		List<String> codes = collectDistinct(rows, codeGetter);
		Set<String> existing = codes.isEmpty() ? Set.of() : existingLoader.apply(codes);
		return CodeUniqueness.builder().duplicatesInFile(duplicatesInFile).existing(existing).build();
	}

	/**
	 * 校验导入状态文案
	 * @param v 行校验器
	 * @param statusLabel Excel 中的状态文案
	 * @return 同一 validator，便于链式
	 */
	public static ImportRowValidator requireEnableStatus(ImportRowValidator v, String statusLabel) {
		if (!v.ok()) {
			return v;
		}

		EnableStatus parsed = EnableStatusLabels.parseImport(statusLabel);
		if (parsed == EnableStatus.UNKNOWN) {
			v.fail(ImportErrors.invalidValue(v.rowNum(), FIELD_STATUS, statusLabel));
		}
		return v;
	}

	/**
	 * 编码唯一性预检结果。
	 */
	@Value
	@Builder
	@Accessors(fluent = true)
	public static class CodeUniqueness {

		Set<String> duplicatesInFile;

		Set<String> existing;

	}

}
