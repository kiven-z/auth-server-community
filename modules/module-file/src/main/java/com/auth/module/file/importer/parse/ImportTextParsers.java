package com.auth.module.file.importer.parse;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.RowOutcome;
import lombok.experimental.UtilityClass;

/**
 * 常用文本字段解析。
 *
 * @author Bunny
 */
@UtilityClass
public class ImportTextParsers {

	/**
	 * 必填文本：trim 后非空
	 * @param raw 原始值
	 * @param rowNum 行号
	 * @param field 字段名
	 * @return 解析结果
	 */
	public static RowOutcome<String> require(String raw, int rowNum, String field) {
		if (CharSequenceUtil.isBlank(raw)) {
			return RowOutcome.err(ImportErrors.required(rowNum, field));
		}
		return RowOutcome.ok(raw.trim());
	}

	/**
	 * 可选文本：blank 时返回 null，否则 trim
	 * @param raw 原始值
	 * @return trim 后的值或 null
	 */
	public static String optional(String raw) {
		return CharSequenceUtil.trimToNull(raw);
	}

}
