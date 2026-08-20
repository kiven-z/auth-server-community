package com.auth.common.data.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户账号状态枚举
 *
 * <p>
 * 与 sys_user 表的 status 字段一一对应
 * </p>
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

	/**
	 * 禁用
	 */
	DISABLED(0, "禁用"),

	/**
	 * 启用
	 */
	NORMAL(1, "启用"),

	/**
	 * 锁定
	 */
	LOCKED(2, "锁定");

	private final int code;

	private final String desc;

	/**
	 * 根据 code 解析枚举
	 * @param code 状态码
	 * @return 匹配的枚举，不存在则返回 null
	 */
	public static UserStatus of(int code) {
		for (UserStatus v : values()) {
			if (v.code == code) {
				return v;
			}
		}
		return null;
	}

	/**
	 * 根据可空 code 解析枚举
	 * @param code 状态码，可能为 null
	 * @return 匹配的枚举；code 为 null 或不存在则返回 null
	 */
	public static UserStatus ofNullable(Integer code) {
		if (code == null) {
			return null;
		}
		return of(code);
	}

	/**
	 * 是否允许进入登录凭证校验（不含登录失败锁定计数）
	 * @return true 仅当状态为启用
	 */
	public boolean allowsLogin() {
		return this == NORMAL;
	}

}