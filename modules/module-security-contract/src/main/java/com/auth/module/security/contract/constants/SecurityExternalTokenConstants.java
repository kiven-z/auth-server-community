package com.auth.module.security.contract.constants;

import lombok.experimental.UtilityClass;

/**
 * 安全令牌约定：外部调用的 HTTP 头、JWT 自定义 claim 名称等
 *
 * @author Bunny
 */
@UtilityClass
public class SecurityExternalTokenConstants {

	/**
	 * JWT 中声明的令牌种类（值为 SecurityTokenKind 枚举的 name()）
	 */
	public static final String TOKEN_TYPE = "token_type";

	/**
	 * 权限版本快照 claim，用于与 Redis AuthProfile.permVersion 等当前版本比对
	 */
	public static final String PERM_VERSION = "perm_version";

}
