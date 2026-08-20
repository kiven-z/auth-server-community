package com.auth.module.security.contract.constants;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;

/**
 * 权限相关常量（契约层） 敏感操作 bypass 语义必须由代码定义（不得远程配置）
 *
 * <p>
 * 业务权限码、通配符的字符级语法见
 * {@link com.auth.module.security.contract.convention.AuthCodeConvention}。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class PermissionConstant {

	/**
	 * 内置超级用户 ID,不得远程注入
	 */
	public static final Set<Long> SUPER_ADMIN_USER_IDS = Set.of(1L);

	/**
	 * 管理员角色码，规定必须是大写，在前端传递和后端传入时全部转成大写 存储时全部大写，返回时全部大写
	 */
	public static final List<String> ADMIN_ROLES = List.of("ADMIN");

	/**
	 * 管理员通配权限集合（命中任意一个即视为拥有所有权限） 权限码忽略大小写，存储时全部小写，返回时也是小写
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
