package com.auth.common.core.utils;

import lombok.experimental.UtilityClass;

/**
 * 树节点 parent_id 约定与规范化。
 *
 * @author Bunny
 */
@UtilityClass
public class TreeParentIdUtil {

	/**
	 * 顶级节点 parent_id（与 sys_dept、sys_menu 等表约定一致）。
	 */
	public static final long ROOT_PARENT_ID = 0L;

	/**
	 * 将 parentId 规范为可比较的父节点 ID（null 或负数视为顶级）。
	 * @param parentId 原始父节点 ID
	 * @return 规范化后的父节点 ID
	 */
	public static long normalize(Long parentId) {
		if (parentId == null || parentId < ROOT_PARENT_ID) {
			return ROOT_PARENT_ID;
		}
		return parentId;
	}

}
