package com.auth.module.file.api.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件删除来源枚举
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum FileDeleteSource {

	/**
	 * 用户自行删除
	 */
	USER_SELF("USER_SELF", RecycleVisibility.USER),

	/**
	 * 管理员删除
	 */
	ADMIN_ACTION("ADMIN_ACTION", RecycleVisibility.ADMIN_ONLY),

	/**
	 * 系统侧删除（存量数据可能仍带此来源码）
	 */
	SYSTEM_ACTION("SYSTEM_ACTION", RecycleVisibility.HIDDEN);

	/**
	 * 已注册删除来源索引
	 */
	private static final Map<String, FileDeleteSource> CODE_INDEX = Arrays.stream(FileDeleteSource.values())
		.collect(Collectors.toUnmodifiableMap(FileDeleteSource::getCode, Function.identity()));

	/**
	 * 删除来源编码
	 */
	private final String code;

	/**
	 * 回收站可见性策略
	 */
	private final RecycleVisibility recycleVisibility;

	/**
	 * 返回用户个人回收站应包含的删除来源编码列表
	 * @return 不可变编码列表
	 */
	public static List<String> userRecycleSourceCodes() {
		return Arrays.stream(FileDeleteSource.values())
			.filter(FileDeleteSource::isUserRecycleVisible)
			.map(FileDeleteSource::getCode)
			.toList();
	}

	/**
	 * 宽松解析删除来源编码
	 * @param code 删除来源编码
	 * @return 解析结果；空值或未知值均返回 {@link Optional#empty()}
	 */
	public static Optional<FileDeleteSource> parse(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(CODE_INDEX.get(code.trim().toUpperCase(Locale.ROOT)));
	}

	/**
	 * 是否对用户个人回收站可见
	 * @return 用户回收站可见时返回 true
	 */
	public boolean isUserRecycleVisible() {
		return recycleVisibility == RecycleVisibility.USER;
	}

}
