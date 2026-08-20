package com.auth.module.file.importer.model;

/**
 * 导入校验错误类型（按校验语义统一编码，不按业务域拆分）
 *
 * @author Bunny
 */
public enum ImportErrorCode {

	/**
	 * 必填字段缺失
	 */
	REQUIRED,

	/**
	 * 文件内重复
	 */
	DUPLICATE_IN_FILE,

	/**
	 * 库中已存在
	 */
	ALREADY_EXISTS,

	/**
	 * 格式不符合约定
	 */
	INVALID_FORMAT,

	/**
	 * 引用目标不存在
	 */
	REFERENCE_NOT_FOUND,

	/**
	 * 值无效（含域特定文案）
	 */
	INVALID_VALUE,

	/**
	 * 长度超出范围
	 */
	LENGTH_OUT_OF_RANGE,

	/**
	 * 导入行数超限
	 */
	ROW_LIMIT_EXCEEDED

}
