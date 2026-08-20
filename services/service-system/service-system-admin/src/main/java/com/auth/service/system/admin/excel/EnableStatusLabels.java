package com.auth.service.system.admin.excel;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * 布尔启用状态在 Excel 中的统一文案（角色、权限、岗位、部门等）。
 *
 * @author Bunny
 */
@UtilityClass
public class EnableStatusLabels {

	/**
	 * 启用状态导出/导入主文案
	 */
	public static final String ENABLED = "启用";

	/**
	 * 禁用状态导出/导入主文案
	 */
	public static final String DISABLED = "禁用";

	private static final Map<String, EnableStatus> IMPORT_BY_LABEL = Map.of(ENABLED, EnableStatus.ENABLED, DISABLED,
			EnableStatus.DISABLED);

	private static final Map<Boolean, String> EXPORT_BY_STATUS = Map.of(true, ENABLED, false, DISABLED);

	/**
	 * 导入文案 → 启用状态（严格匹配 {@link #ENABLED} / {@link #DISABLED}）。
	 * @param label Excel 中填写的状态文案
	 * @return 解析结果；未知或空白为 {@link EnableStatus#UNKNOWN}
	 */
	public static EnableStatus parseImport(String label) {
		if (CharSequenceUtil.isBlank(label)) {
			return EnableStatus.UNKNOWN;
		}
		return IMPORT_BY_LABEL.getOrDefault(label.trim(), EnableStatus.UNKNOWN);
	}

	/**
	 * 布尔启用状态 → 导出文案。
	 * @param status 布尔启用状态
	 * @return 启用/禁用
	 */
	public static String exportLabel(Boolean status) {
		return status != null && status ? EXPORT_BY_STATUS.get(true) : EXPORT_BY_STATUS.get(false);
	}

}
