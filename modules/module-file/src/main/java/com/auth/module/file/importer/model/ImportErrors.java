package com.auth.module.file.importer.model;

import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * {@link ImportRowError} 工厂：按错误类型生成中文 message
 *
 * @author Bunny
 */
@UtilityClass
public class ImportErrors {

	/**
	 * 按错误类型与参数构建行级错误。
	 * @param rowNum 行号（1-based，不含表头；行数超限为 0）
	 * @param code 错误类型
	 * @param args 类型相关参数，见各 * 工厂方法
	 * @return 行级错误
	 */
	public static ImportRowError of(int rowNum, ImportErrorCode code, Object... args) {
		return switch (code) {
			case REQUIRED -> required(rowNum, (String) args[0]);
			case DUPLICATE_IN_FILE -> duplicateInFile(rowNum, (String) args[0], (String) args[1]);
			case ALREADY_EXISTS -> alreadyExists(rowNum, (String) args[0], (String) args[1]);
			case INVALID_FORMAT -> invalidFormat(rowNum, (String) args[0], (String) args[1]);
			case REFERENCE_NOT_FOUND -> referenceNotFound(rowNum, (String) args[0], (String) args[1]);
			case INVALID_VALUE -> invalidValue(rowNum, (String) args[0], (String) args[1]);
			case LENGTH_OUT_OF_RANGE -> lengthOutOfRange(rowNum, (String) args[0], (int) args[1], (int) args[2]);
			case ROW_LIMIT_EXCEEDED -> rowLimitExceeded((int) args[0]);
		};
	}

	/**
	 * 必填字段缺失
	 * @param rowNum 行号
	 * @param field 字段名
	 * @return 行级错误
	 */
	public static ImportRowError required(int rowNum, String field) {
		String message = "第" + rowNum + "行：" + field + "不能为空";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.REQUIRED)
			.args(List.of(field))
			.build();
	}

	/**
	 * 文件内单字段重复
	 * @param rowNum 行号
	 * @param field 字段名
	 * @param value 字段值
	 * @return 行级错误
	 */
	public static ImportRowError duplicateInFile(int rowNum, String field, String value) {
		String message = "第" + rowNum + "行：" + field + "「" + value + "」在文件内重复";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.DUPLICATE_IN_FILE)
			.args(List.of(field, value))
			.build();
	}

	/**
	 * 岗位复合键在文件内重复
	 * @param rowNum 行号
	 * @param postCode 岗位编码
	 * @param deptCode 部门编码
	 * @return 行级错误
	 */
	public static ImportRowError duplicatePostInFile(int rowNum, String postCode, String deptCode) {
		String message = "第" + rowNum + "行：postCode「" + postCode + "」在文件内重复（deptCode「" + deptCode + "」）";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.DUPLICATE_IN_FILE)
			.args(List.of(postCode, deptCode))
			.build();
	}

	/**
	 * 库中已存在（单字段）
	 * @param rowNum 行号
	 * @param field 字段名
	 * @param value 字段值
	 * @return 行级错误
	 */
	public static ImportRowError alreadyExists(int rowNum, String field, String value) {
		String message = "第" + rowNum + "行：" + field + "「" + value + "」已存在";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.ALREADY_EXISTS)
			.args(List.of(field, value))
			.build();
	}

	/**
	 * 岗位复合键在库中已存在
	 * @param rowNum 行号
	 * @param postCode 岗位编码
	 * @param deptCode 部门编码
	 * @return 行级错误
	 */
	public static ImportRowError alreadyExistsPost(int rowNum, String postCode, String deptCode) {
		String message = "第" + rowNum + "行：postCode「" + postCode + "」已存在（deptCode「" + deptCode + "」）";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.ALREADY_EXISTS)
			.args(List.of(postCode, deptCode))
			.build();
	}

	/**
	 * 格式不符合约定
	 * @param rowNum 行号
	 * @param field 字段名
	 * @param value 字段值
	 * @return 行级错误
	 */
	public static ImportRowError invalidFormat(int rowNum, String field, String value) {
		String message = "第" + rowNum + "行：" + field + "「" + value + "」格式不符合约定";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.INVALID_FORMAT)
			.args(List.of(field, value))
			.build();
	}

	/**
	 * 引用目标不存在
	 * @param rowNum 行号
	 * @param field 字段名
	 * @param value 字段值
	 * @return 行级错误
	 */
	public static ImportRowError referenceNotFound(int rowNum, String field, String value) {
		String message = "第" + rowNum + "行：" + field + "「" + value + "」不存在";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.REFERENCE_NOT_FOUND)
			.args(List.of(field, value))
			.build();
	}

	/**
	 * 引用目标存在但不可用（停用或祖先链不可用）
	 * @param rowNum 行号
	 * @param field 字段名
	 * @param value 字段值
	 * @return 行级错误
	 */
	public static ImportRowError referenceUnavailable(int rowNum, String field, String value) {
		String message = "第" + rowNum + "行：" + field + "「" + value + "」不存在或不可用";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.INVALID_VALUE)
			.args(List.of(field, value))
			.build();
	}

	/**
	 * 通用无效值
	 * @param rowNum 行号
	 * @param field 字段名
	 * @param value 字段值
	 * @return 行级错误
	 */
	public static ImportRowError invalidValue(int rowNum, String field, String value) {
		String message = "第" + rowNum + "行：" + field + "「" + value + "」无效";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.INVALID_VALUE)
			.args(List.of(field, value))
			.build();
	}

	/**
	 * 用户状态必填（域特定文案）
	 * @param rowNum 行号
	 * @return 行级错误
	 */
	public static ImportRowError statusRequired(int rowNum) {
		String message = "第" + rowNum + "行：status不能为空";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.INVALID_VALUE)
			.args(List.of("status"))
			.build();
	}

	/**
	 * 生日格式无效（域特定文案）
	 * @param rowNum 行号
	 * @param value 字段值
	 * @return 行级错误
	 */
	public static ImportRowError birthdayInvalid(int rowNum, String value) {
		String message = "第" + rowNum + "行：birthday「" + value + "」无效，应为 yyyy-MM-dd";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.INVALID_VALUE)
			.args(List.of("birthday", value))
			.build();
	}

	/**
	 * 字段长度超出范围
	 * @param rowNum 行号
	 * @param field 字段名
	 * @param min 最小长度
	 * @param max 最大长度
	 * @return 行级错误
	 */
	public static ImportRowError lengthOutOfRange(int rowNum, String field, int min, int max) {
		String message = "第" + rowNum + "行：" + field + "长度须在 " + min + "～" + max + " 之间";
		return ImportRowError.builder()
			.rowIndex(rowNum)
			.message(message)
			.code(ImportErrorCode.LENGTH_OUT_OF_RANGE)
			.args(List.of(field, min, max))
			.build();
	}

	/**
	 * 导入行数超限
	 * @param max 最大行数
	 * @return 行级错误
	 */
	public static ImportRowError rowLimitExceeded(int max) {
		String message = "导入行数超过上限：最多 " + max + " 行";
		return ImportRowError.builder()
			.rowIndex(0)
			.message(message)
			.code(ImportErrorCode.ROW_LIMIT_EXCEEDED)
			.args(List.of(max))
			.build();
	}

}
