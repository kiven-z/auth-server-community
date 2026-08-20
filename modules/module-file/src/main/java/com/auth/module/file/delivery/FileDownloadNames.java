package com.auth.module.file.delivery;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * 下载文件名：UTC 时间戳与前缀拼装
 *
 * @author Bunny
 */
@UtilityClass
public class FileDownloadNames {

	private static final DateTimeFormatter STAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
		.withZone(ZoneOffset.UTC);

	/**
	 * 生成 UTC 时间戳片段
	 * @param instant 时刻
	 * @return 形如 20260811143022
	 */
	public static String stamp(Instant instant) {
		Objects.requireNonNull(instant, "instant must not be null");
		return STAMP_FORMATTER.format(instant);
	}

	/**
	 * 拼装 {@code prefix_stamp.ext}
	 * @param prefix 文件名前缀（不含扩展名）
	 * @param instant 时刻
	 * @param extension 扩展名（可带或不带点）
	 * @return 完整文件名
	 */
	public static String of(String prefix, Instant instant, String extension) {
		String normalizedPrefix = requirePrefix(prefix);
		String normalizedExt = normalizeExtension(extension);
		return normalizedPrefix + "_" + stamp(instant) + "." + normalizedExt;
	}

	/**
	 * 拼装批量 ZIP 文件名 {@code prefix_stamp.zip}
	 * @param prefix 文件名前缀（如 file-records）
	 * @return 形如 file-records_20260811143022.zip
	 */
	public static String batchZip(String prefix) {
		return of(prefix, Instant.now(), "zip");
	}

	private static String requirePrefix(String prefix) {
		String trimmed = CharSequenceUtil.trim(prefix);
		if (CharSequenceUtil.isBlank(trimmed)) {
			throw new IllegalArgumentException("prefix must not be blank");
		}
		return trimmed;
	}

	private static String normalizeExtension(String extension) {
		String trimmed = CharSequenceUtil.trim(extension);
		if (CharSequenceUtil.isBlank(trimmed)) {
			throw new IllegalArgumentException("extension must not be blank");
		}
		String withoutDot = trimmed.startsWith(".") ? trimmed.substring(1) : trimmed;
		if (CharSequenceUtil.isBlank(withoutDot)) {
			throw new IllegalArgumentException("extension must not be blank");
		}
		return withoutDot.toLowerCase(Locale.ROOT);
	}

}
