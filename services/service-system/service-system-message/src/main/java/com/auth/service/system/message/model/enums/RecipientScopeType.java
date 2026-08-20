package com.auth.service.system.message.model.enums;

/**
 * 站内信接收范围类型
 *
 * @author Bunny
 */
public enum RecipientScopeType {

	/**
	 * 指定用户
	 */
	USER,

	/**
	 * 指定岗位
	 */
	POST,

	/**
	 * 指定部门
	 */
	DEPT,

	/**
	 * 全员
	 */
	ALL;

	/**
	 * 解析范围类型（大小写不敏感）
	 * @param raw 原始字符串
	 * @return 枚举；无法识别时返回 null
	 */
	public static RecipientScopeType from(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return RecipientScopeType.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * 是否读扩散（全员公开）
	 * @return true=ALL
	 */
	public boolean isPull() {
		return this == ALL;
	}

}
