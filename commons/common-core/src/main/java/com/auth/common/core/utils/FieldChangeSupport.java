package com.auth.common.core.utils;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;

/**
 * 管理端实体更新前后的字段变更判断
 *
 * @author Bunny
 */
@UtilityClass
public class FieldChangeSupport {

	/**
	 * 业务编码类字符串是否发生变化（双方 trim 后比较）
	 * @param before 更新前值
	 * @param after 更新后值
	 * @return 编码是否变化
	 */
	public static boolean codeChanged(String before, String after) {
		return !CharSequenceUtil.equals(CharSequenceUtil.trim(before), CharSequenceUtil.trim(after));
	}

	/**
	 * 通用可空字段是否发生变化（如 status、isDeleted、枚举码）
	 * @param before 更新前值
	 * @param after 更新后值
	 * @return 字段是否变化
	 */
	public static boolean valueChanged(Object before, Object after) {
		return !Objects.equals(before, after);
	}

	/**
	 * 是否任一变更标志为 true
	 * @param changeFlags 各字段变更结果
	 * @return 是否存在任一变更
	 */
	public static boolean anyChanged(boolean... changeFlags) {
		for (boolean changed : changeFlags) {
			if (changed) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 编码重命名场景下收集旧、新编码列表（trim 后去重；未改名时仅含一个编码）
	 * @param before 更新前编码
	 * @param after 更新后编码
	 * @return 不可变列表，顺序为旧码、新码（相同时仅一项）
	 */
	public static List<String> renameCodes(String before, String after) {
		String oldCode = CharSequenceUtil.trim(before);
		String newCode = CharSequenceUtil.trim(after);
		if (CharSequenceUtil.equals(oldCode, newCode)) {
			return List.of(oldCode);
		}
		return List.of(oldCode, newCode);
	}

}
