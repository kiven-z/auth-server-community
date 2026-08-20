package com.auth.service.system.schedule.model.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_TIME_ZONE_UNSUPPORTED;

/**
 * 定时任务允许的调度时区（IANA ZoneId 白名单）
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum SysJobTimeZone {

	/**
	 * 东八区（默认）
	 */
	ASIA_SHANGHAI("Asia/Shanghai"),

	/**
	 * 协调世界时
	 */
	UTC("UTC"),

	/**
	 * 香港
	 */
	ASIA_HONG_KONG("Asia/Hong_Kong"),

	/**
	 * 东京
	 */
	ASIA_TOKYO("Asia/Tokyo"),

	/**
	 * 新加坡
	 */
	ASIA_SINGAPORE("Asia/Singapore"),

	/**
	 * 伦敦
	 */
	EUROPE_LONDON("Europe/London"),

	/**
	 * 纽约
	 */
	AMERICA_NEW_YORK("America/New_York");

	/**
	 * 未填写时的默认 ZoneId
	 */
	public static final String DEFAULT_ZONE_ID = ASIA_SHANGHAI.zoneId;

	private static final Map<String, SysJobTimeZone> BY_ZONE_ID = Arrays.stream(values())
		.collect(Collectors.toUnmodifiableMap(SysJobTimeZone::getZoneId, Function.identity()));

	private final String zoneId;

	/**
	 * 归一化任务时区：空白用默认，非法抛业务异常
	 * @param raw 原始 ZoneId
	 * @return 白名单内 ZoneId 字符串
	 */
	public static String normalize(String raw) {
		if (CharSequenceUtil.isBlank(raw)) {
			return DEFAULT_ZONE_ID;
		}
		SysJobTimeZone matched = BY_ZONE_ID.get(raw.trim());
		if (matched == null) {
			throw new SysJobException(JOB_TIME_ZONE_UNSUPPORTED, raw);
		}
		return matched.zoneId;
	}

}
