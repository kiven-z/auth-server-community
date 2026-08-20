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
	DEPT_AND_CHILD,

	/**
	 * 注解占位：表示运行期按用户画像裁决，不作为库表存储值。
	 */
	FROM_PROFILE;

	/**
	 * 解析库表 scope_type 或 JSON 字符串（仅存储层类型）
	 * @param raw 原始值
	 * @return 枚举；无法识别时为 null
	 */
	public static DataScopeStorageType parse(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			DataScopeStorageType parsed = valueOf(raw.strip().toUpperCase(Locale.ROOT));
			return parsed == FROM_PROFILE ? null : parsed;
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

}
