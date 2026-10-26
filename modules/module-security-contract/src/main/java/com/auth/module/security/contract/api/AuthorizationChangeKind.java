package com.auth.module.security.contract.api;

/**
 * 授权变更维度
 *
 * @author Bunny
 */
public enum AuthorizationChangeKind {

	/**
	 * 角色相关
	 */
	ROLE,

	/**
	 * 权限码相关
	 */
	PERMISSION,

	/**
	 * grant_table 授权边
	 */
	GRANT,

	/**
	 * 用户-部门关系
	 */
	USER_DEPT,

	/**
	 * 用户-岗位关系
	 */
	USER_POST,

	/**
	 * 用户直连失效
	 * <p>
	 * 主档、数据范围等按 userId 刷画像
	 * </p>
	 */
	USER

}
