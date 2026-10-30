package com.auth.module.security.contract.profile.datascope;

import java.util.Locale;

/**
 * 数据权限维度
 *
 * @author Bunny
 */
public enum DataScopeDimension {

	/**
	 * 部门维（树形结构，支持 IDS_AND_CHILD 展开）
	 */
	DEPT;

	/**
	 * 解析库表 dimension 或表单字符串
	 * @param raw 原始值
	 * @return 枚举；无法识别时为 null
	 */
	public static DataScopeDimension parse(String raw) {
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

	/**
	 * 是否为树形结构维度（决定 IDS_AND_CHILD 是否需要闭包展开）
	 * @return 树形维度为 true
	 */
	public boolean isTree() {
		return switch (this) {
			case DEPT -> true;
			// 示例
			// case REGION -> false;
			default -> false;
		};
	}

}
