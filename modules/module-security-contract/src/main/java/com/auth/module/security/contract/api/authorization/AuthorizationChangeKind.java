package com.auth.module.security.contract.api.authorization;

/**
 * 授权变更维度（跨服务失效契约）：与影响面反查入口对应关系大致如下
 * <ul>
 * <li>{@link #ROLE} — sys_role、role_scope、批量 roleCode</li>
 * <li>{@link #PERMISSION} — sys_permission、sys_role_permission、permissionCodes</li>
 * <li>{@link #GRANT} — grant_table、GrantSubject 列表</li>
 * <li>{@link #USER_DEPT} — user_dept、部门成员 deptIds</li>
 * <li>{@link #USER_POST} — user_post、岗位人员 postIds</li>
 * <li>{@link #USER} — 用户直连失效（sys_user、user_scope 等，通常直指 userId）</li>
 * </ul>
 *
 * @author Bunny
 */
public enum AuthorizationChangeKind {

	/**
	 * 角色相关
	 */
	ROLE("role"),

	/**
	 * 权限码相关
	 */
	PERMISSION("perm"),

	/**
	 * grant_table 授权边
	 */
	GRANT("grant"),

	/**
	 * 用户-部门关系
	 */
	USER_DEPT("dept"),

	/**
	 * 用户-岗位关系
	 */
	USER_POST("post"),

	/**
	 * 用户直连失效（主档、数据范围等按 userId 刷画像）
	 */
	USER("user");

	/**
	 * 失效事件 ID 短前缀（格式 prefix:snowflakeId）
	 */
	private final String eventIdPrefix;

	AuthorizationChangeKind(String eventIdPrefix) {
		this.eventIdPrefix = eventIdPrefix;
	}

	/**
	 * 返回失效事件 ID 短前缀
	 * @return 短前缀，不含冒号
	 */
	public String eventIdPrefix() {
		return eventIdPrefix;
	}

}
