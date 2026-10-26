package com.auth.module.security.contract.constants;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * 权限码、角色码等安全侧编码格式约定
 *
 * @author Bunny
 */
@UtilityClass
public class AuthCodeConvention {

	/**
	 * 权限码单段
	 */
	public static final String PERMISSION_CODE_SEGMENT_REGEX = "(?:\\*|[a-z][a-z0-9]*)";

	/**
	 * 权限码整体：1～4 段
	 * <p>
	 * 示例：*、*:*、*:*:*、*:*:*:*、sys、sys:dept、sys:dept:query、sys:file:recycle:query
	 * </p>
	 */
	public static final String PERMISSION_CODE_REGEX = "^" + PERMISSION_CODE_SEGMENT_REGEX + "(?::"
			+ PERMISSION_CODE_SEGMENT_REGEX + "){0,3}$";

	private static final Pattern PERMISSION_CODE_PATTERN = Pattern.compile(PERMISSION_CODE_REGEX);

	/**
	 * 角色码：大写字母与下划线，且首字符为大写字母
	 * <p>
	 * 示例：ADMIN、SYS_ADMIN
	 * </p>
	 */
	public static final String ROLE_CODE_REGEX = "^[A-Z][A-Z_]*$";

	private static final Pattern ROLE_CODE_PATTERN = Pattern.compile(ROLE_CODE_REGEX);

	/**
	 * 是否为约定内的权限码格式
	 * @param permissionCode 权限码
	 * @return 是否符合
	 */
	public static boolean isWellFormedPermissionCode(String permissionCode) {
		return permissionCode != null && !permissionCode.isEmpty()
				&& PERMISSION_CODE_PATTERN.matcher(permissionCode).matches();
	}

	/**
	 * 是否为约定内的角色码格式
	 * @param roleCode 角色码
	 * @return 是否符合
	 */
	public static boolean isWellFormedRoleCode(String roleCode) {
		return roleCode != null && !roleCode.isEmpty() && ROLE_CODE_PATTERN.matcher(roleCode).matches();
	}

}
