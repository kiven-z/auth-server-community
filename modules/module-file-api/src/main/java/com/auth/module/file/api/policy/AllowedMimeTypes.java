package com.auth.module.file.api.policy;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * 上传 MIME 白名单常量组，供 {@link FileBizType} 组合复用。
 *
 * @author Bunny
 */
@UtilityClass
public class AllowedMimeTypes {

	/**
	 * 常见位图（不含 GIF）
	 */
	public static final Set<String> RASTER_IMAGE = Set.of("image/jpeg", "image/png", "image/webp");

	/**
	 * 常见位图（含 GIF）
	 */
	public static final Set<String> RASTER_IMAGE_WITH_GIF = Set.of("image/jpeg", "image/png", "image/gif",
			"image/webp");

	/**
	 * 通用文档与压缩包
	 */
	public static final Set<String> OFFICE_DOCUMENTS = Set.of("application/pdf", "text/plain", "text/csv",
			"application/zip", "application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation");

}
