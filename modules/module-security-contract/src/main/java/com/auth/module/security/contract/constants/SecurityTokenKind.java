package com.auth.module.security.contract.constants;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 令牌类型（从请求上下文中解析）
 *
 * @author Bunny
 */
public enum SecurityTokenKind {

	/**
	 * 外部访问
	 */
	EXTERNAL_ACCESS,

	/**
	 * 外部刷新
	 */
	EXTERNAL_REFRESH,

	/**
	 * 内部访问
	 */
	INTERNAL,;

	/**
	 * 获取令牌类型
	 * @param token 令牌
	 * @return 令牌类型
	 */
	public static SecurityTokenKind of(String token) {
		if (CharSequenceUtil.isBlank(token)) {
			return null;
		}
		try {
			return SecurityTokenKind.valueOf(token);
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

}