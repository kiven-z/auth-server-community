package com.auth.service.system.admin.model.constants;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

import java.util.Set;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_INVALID;

/**
 * 当前用户 UI 偏好配置键白名单
 *
 * @author Bunny
 */
@UtilityClass
public class UserPreferenceKeys {

	/**
	 * 界面语言
	 */
	public static final String UI_LOCALE = "ui.locale";

	/**
	 * 颜色方案 + 侧栏皮肤 + 品牌主色
	 */
	public static final String UI_THEME = "ui.theme";

	/**
	 * 导航布局模式 + 侧栏展开状态
	 */
	public static final String UI_LAYOUT = "ui.layout";

	/**
	 * 界面显示开关 + 标签风格 + 页宽
	 */
	public static final String UI_DISPLAY = "ui.display";

	/**
	 * 多标签页快照（跨浏览器恢复）
	 */
	public static final String UI_TAGS = "ui.tags";

	/**
	 * 允许通过个人中心 API 读写的配置键集合
	 */
	public static final Set<String> ALLOWED_KEYS = Set.of(UI_LOCALE, UI_THEME, UI_LAYOUT, UI_DISPLAY, UI_TAGS);

	/**
	 * 校验 upsert 配置键在白名单内，且配置值为非空 JSON 对象
	 * @param configKey 配置键
	 * @param configValue 配置值
	 * @throws SystemBusinessException 如果配置键不在白名单内，或者配置值为空
	 */
	public static void assertUpsertForm(String configKey, JsonNode configValue) {
		if (!ALLOWED_KEYS.contains(configKey)) {
			throw new SystemBusinessException(DATA_INVALID, configKey);
		}
		if (!configValue.isObject() || configValue.isEmpty()) {
			throw new SystemBusinessException(DATA_INVALID, configValue);
		}
	}

}
