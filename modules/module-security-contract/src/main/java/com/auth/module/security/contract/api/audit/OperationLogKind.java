package com.auth.module.security.contract.api.audit;

/**
 * 操作日志类型。
 *
 * @author Bunny
 */
public enum OperationLogKind {

	/**
	 * 新增
	 */
	CREATE,

	/**
	 * 更新
	 */
	UPDATE,

	/**
	 * 删除
	 */
	DELETE,

	/**
	 * 查询
	 */
	QUERY,

	/**
	 * 导出
	 */
	EXPORT

}
