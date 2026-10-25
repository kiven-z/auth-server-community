package com.auth.module.security.contract.api.datascope;

import java.util.Locale;

/**
 * 数据范围存储层枚举
 *
 * @author Bunny
 */
public enum DataScopeStorageType {

	/**
	 * 全部可见
	 */
	ALL,

	/**
	 * 仅本人
	 */
	SELF,

	/**
	 * 指定部门（不含子部门）
	 */
	DEPT,

	/**
	 * 指定部门及全部后代（登录解析时需展开为部门 ID 列表）
	 */
	DEPT_AND_CHILD;

	/**
	 * 解析库表 scope_type 或 JSON 字符串
	 * @param raw 原始值
	 * @return 枚举；无法识别时为 null
	 */
	public static DataScopeStorageType parse(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return valueOf(raw.strip().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

}
