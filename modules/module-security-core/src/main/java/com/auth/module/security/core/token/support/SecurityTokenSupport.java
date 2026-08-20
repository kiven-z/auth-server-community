package com.auth.module.security.core.token.support;

import lombok.experimental.UtilityClass;

/**
 * 安全令牌支持
 *
 * @author Bunny
 */
@UtilityClass
public class SecurityTokenSupport {

	/**
	 * 解析权限版本快照
	 * @param raw 原始对象
	 * @return 权限版本快照
	 */
	public static Long parsePermVersionClaim(Object raw) {
		if (raw == null) {
			return null;
		}
		if (raw instanceof Number n) {
			return n.longValue();
		}
		if (raw instanceof String s) {
			try {
				return Long.valueOf(s.trim());
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}
		return null;
	}

}
