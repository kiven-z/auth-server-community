package com.auth.module.file.importer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 单行导入失败信息
 *
 * @author Bunny
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@Value
@Builder
@EqualsAndHashCode
@Accessors(fluent = true)
public class ImportRowError {

	/**
	 * 行号（行数超限为 0）
	 */
	int rowIndex;

	/**
	 * 失败原因（保持历史格式）
	 */
	String message;

	/**
	 * 错误类型
	 */
	ImportErrorCode code;

	/**
	 * 错误参数
	 */
	List<Object> args;

}
