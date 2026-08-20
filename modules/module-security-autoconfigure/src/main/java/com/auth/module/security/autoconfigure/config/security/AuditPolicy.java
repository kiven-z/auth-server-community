package com.auth.module.security.autoconfigure.config.security;

/**
 * 审计策略
 *
 * @author Bunny
 */
public enum AuditPolicy {

	/**
	 * 无审计（关闭）
	 */
	NONE,

	/**
	 * 访问公开接口不记录
	 */
	PUBLIC_NO_RECORD,

	/**
	 * 访问公开接口只要携带Token就记录
	 */
	PUBLIC_WITH_TOKEN_RECORD,

	/**
	 * 全量审计（只要访问就记录）
	 */
	ALL_RECORD,

}
