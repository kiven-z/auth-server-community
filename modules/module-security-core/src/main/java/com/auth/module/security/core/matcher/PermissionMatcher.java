package com.auth.module.security.core.matcher;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.auth.module.security.contract.constants.PermissionConstant.isAdminPermission;

/**
 * 权限匹配器，支持分段通配符
 *
 * @author Bunny
 */
@UtilityClass
public class PermissionMatcher {

	private static final String ASTERISK = "*";

	/**
	 * 匹配权限
	 * @param granted 授予的权限
	 * @param required 需要的权限
	 * @return 是否匹配
	 */
	public static boolean matches(String granted, String required) {
		// 如果授予的权限或需要的权限为空，则返回 false
		if (CharSequenceUtil.isBlank(granted) || CharSequenceUtil.isBlank(required)) {
			return false;
		}

		// 修剪并转换为小写
		granted = CharSequenceUtil.trim(granted).toLowerCase();
		required = CharSequenceUtil.trim(required).toLowerCase();

		// 检查是否为管理员通配权限
		if (isAdminPermission(List.of(granted))) {
			return true;
		}

		// 如果授予的权限和需要的权限相等，则返回 true
		if (granted.equals(required)) {
			return true;
		}

		// 分割授予的权限和需要的权限
		String[] gParts = granted.split(":");
		String[] rParts = required.split(":");

		// 获取授予的权限和需要的权限的最大长度
		int max = Math.max(gParts.length, rParts.length);

		// 遍历最大长度
		for (int i = 0; i < max; i++) {
			String gp = i < gParts.length ? gParts[i] : "";
			String rp = i < rParts.length ? rParts[i] : "";
			if (gp.equals(ASTERISK)) {
				continue;
			}
			if (!gp.equals(rp)) {
				return false;
			}
		}
		return true;
	}

}
