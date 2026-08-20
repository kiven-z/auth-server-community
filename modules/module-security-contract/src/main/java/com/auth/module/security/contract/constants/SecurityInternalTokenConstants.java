package com.auth.module.security.contract.constants;

import lombok.experimental.UtilityClass;

/**
 * 安全令牌约定：内部调用的 HTTP 头、JWT 自定义 claim 名称等
 *
 * @author Bunny
 */
@UtilityClass
public class SecurityInternalTokenConstants {

	/**
	 * 内部 JWT 请求头名称
	 */
	public static final String INTERNAL_HEADER = "X-Internal-JWT";

	/**
	 * 内部 JWT 最大 TTL（秒）
	 */
	public static final long INTERNAL_MAX_TTL_SECONDS = 60L;

	/**
	 * 服务身份令牌中携带的服务名 claim（一般取 spring.application.name）
	 */
	public static final String SERVICE_ID = "service_id";

	/**
	 * 服务身份令牌的 sub 占位值（数值型 subject 要求下的占位，无业务含义）
	 */
	public static final Long SERVICE_SUB_PLACEHOLDER = 0L;

	/**
	 * 服务身份令牌写入 SecurityContext 时使用的角色（不带 ROLE_ 前缀，由权限填充器拼接）
	 */
	public static final String ROLE_INTERNAL_SERVICE = "INTERNAL_SERVICE";

	/**
	 * 内部令牌主体类型：用户身份
	 */
	public static final String PRINCIPAL_TYPE_USER = "USER";

	/**
	 * 内部令牌主体类型：服务身份（无用户上下文的内部互调）
	 */
	public static final String PRINCIPAL_TYPE_SERVICE = "SERVICE";

	/**
	 * 内部令牌主体类型 claim：USER（用户身份）/ SERVICE（服务身份）
	 */
	public static final String PRINCIPAL_TYPE = "principal_type";

}
