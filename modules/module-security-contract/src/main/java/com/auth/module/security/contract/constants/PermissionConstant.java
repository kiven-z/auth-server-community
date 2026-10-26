package com.auth.module.security.contract.constants;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;

/**
 * 权限配置特殊常量
 *
 * @author Bunny
 */
@UtilityClass
public class PermissionConstant {

	/**
	 * 内置超级用户 ID
	 */
	public static final Set<Long> SUPER_ADMIN_USER_IDS = Set.of(1L);

	/**
	 * 管理员角色码（大写）,指定哪些角色码为超管
	 */
	public static final List<String> ADMIN_ROLES = List.of("ADMIN");

	/**
	 * 管理员通配权限集合（权限码忽略大小写）
	 */
	public static final List<String> ADMIN_WILDCARD_PERMISSIONS = List.of("*", "*:*", "*:*:*", "*:*:*:*");

	/**
	 * 是否是超级管理员
	 * @param userId 用户 ID
	 * @return 是否是超级管理员
	 */
	public static boolean isSuperAdmin(Long userId) {
		return SUPER_ADMIN_USER_IDS.contains(userId);
	}

	/**
	 * 是否是管理员角色
	 * @param userRoles 角色列表
	 * @return 是否是管理员角色
	 */
	public static boolean isAdminRole(List<String> userRoles) {
		List<String> roles = userRoles.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(CharSequenceUtil::trim)
			.toList();

		return ADMIN_ROLES.stream().anyMatch(w -> roles.stream().anyMatch(w::equals));
	}

	/**
	 * 是否是管理员权限
	 * @param userPermissions 权限列表
	 * @return 是否是管理员权限
	 */
	public static boolean isAdminPermission(List<String> userPermissions) {
		List<String> permissions = userPermissions.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(CharSequenceUtil::trim)
			.toList();

		return ADMIN_WILDCARD_PERMISSIONS.stream().anyMatch(w -> permissions.stream().anyMatch(w::equals));
	}

	/**
	 * 是否是管理员（功能权限语义）
	 * @param userId 用户ID
	 * @param roles 角色列表
	 * @return 是否是管理员
	 */
	public boolean isAdmin(Long userId, List<String> roles) {
		return isSuperAdmin(userId) || isAdminRole(roles);
	}

}
