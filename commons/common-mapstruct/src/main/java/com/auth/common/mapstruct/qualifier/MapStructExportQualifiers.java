package com.auth.common.mapstruct.qualifier;

import lombok.experimental.UtilityClass;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Excel 导出列映射辅助方法
 *
 * @author Bunny
 */
@UtilityClass
public class MapStructExportQualifiers {

	/**
	 * 导出时间列默认格式
	 */
	public static final DateTimeFormatter EXPORT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		.withZone(ZoneOffset.UTC);

	/**
	 * 启用状态 → 导出文案
	 * @param status 布尔状态
	 * @return 中文标签
	 */
	@Named("statusLabel")
	public String statusLabel(Boolean status) {
		return status != null && status ? "启用" : "禁用";
	}

	/**
	 * 计算有效 → 导出文案
	 * @param effective 计算有效标志
	 * @return 中文标签
	 */
	@Named("effectiveLabel")
	public String effectiveLabel(Boolean effective) {
		return effective != null && effective ? "有效" : "无效";
	}

	/**
	 * 时间 → 导出文本
	 * @param instant 时间
	 * @return 格式化文本，空则返回空串
	 */
	@Named("dateTimeText")
	public String dateTimeText(Instant instant) {
		return instant == null ? "" : EXPORT_DATE_TIME.format(instant);
	}

}
