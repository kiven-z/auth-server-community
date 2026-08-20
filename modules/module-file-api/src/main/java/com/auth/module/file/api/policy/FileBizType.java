package com.auth.module.file.api.policy;

import com.auth.module.file.api.model.enums.VisibilityRule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件业务类型枚举（含上传类型/大小策略）。
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum FileBizType {

	/**
	 * 用户头像
	 */
	AVATAR("avatar", VisibilityRule.FORCE_PUBLIC, 2, AllowedMimeTypes.RASTER_IMAGE),

	/**
	 * 消息模板正文配图（Markdown / 富文本公开访问）
	 */
	MESSAGE_IMAGE("message-image", VisibilityRule.FORCE_PUBLIC, 5, AllowedMimeTypes.RASTER_IMAGE_WITH_GIF),

	/**
	 * 通用附件
	 */
	ATTACHMENT("attachment", VisibilityRule.DEFAULT_PRIVATE, 20,
			Stream.concat(AllowedMimeTypes.RASTER_IMAGE_WITH_GIF.stream(), AllowedMimeTypes.OFFICE_DOCUMENTS.stream())
				.collect(Collectors.toUnmodifiableSet()));

	/**
	 * 已注册业务类型索引
	 */
	private static final Map<String, FileBizType> CODE_INDEX = Arrays.stream(FileBizType.values())
		.collect(Collectors.toUnmodifiableMap(FileBizType::getCode, Function.identity()));

	/**
	 * 业务类型编码
	 */
	private final String code;

	/**
	 * 可见性规则
	 */
	private final VisibilityRule visibilityRule;

	/**
	 * 单文件大小上限（MB）
	 */
	private final int maxSizeMb;

	/**
	 * 允许的 MIME 类型白名单（按魔数检测结果校验）
	 */
	private final Set<String> allowedMimeTypes;

	/**
	 * 严格解析业务类型编码
	 * @param code 业务类型编码
	 * @return 业务类型枚举
	 * @throws IllegalArgumentException 编码为空或编码不存在时抛出
	 */
	public static FileBizType require(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("File biz type code must not be blank");
		}
		return Optional.ofNullable(CODE_INDEX.get(code.trim().toLowerCase(Locale.ROOT)))
			.orElseThrow(() -> new IllegalArgumentException("Unsupported file biz type code: " + code));
	}

	/**
	 * 单文件大小上限（字节）
	 * @return 字节上限
	 */
	public long maxSizeBytes() {
		return maxSizeMb * 1024L * 1024L;
	}

	/**
	 * 判断 MIME 是否在当前业务类型白名单内
	 * @param mimeType 检测到的 MIME（可含 charset 等参数）
	 * @return 允许则 true
	 */
	public boolean allowsMimeType(String mimeType) {
		if (mimeType == null || mimeType.isBlank()) {
			return false;
		}
		String normalized = mimeType.trim().toLowerCase(Locale.ROOT);
		int separatorIndex = normalized.indexOf(';');
		if (separatorIndex >= 0) {
			normalized = normalized.substring(0, separatorIndex).trim();
		}
		return allowedMimeTypes.contains(normalized);
	}

}
