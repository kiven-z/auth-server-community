package com.auth.module.file.api.model.enums;

import java.util.Locale;

/**
 * 文件存储平台枚举
 *
 * @author Bunny
 */
public enum StoragePlatformEnum {

	/**
	 * Minio
	 */
	MINIO,

	/**
	 * 阿里云 OSS
	 */
	ALIYUN_OSS;

	/**
	 * 解析请求中的平台参数
	 * @param platform 平台字符串
	 * @return 平台枚举；当入参为空时返回 null
	 * @throws IllegalArgumentException 平台值无法识别时
	 */
	public static StoragePlatformEnum fromNullable(String platform) {
		if (platform == null || platform.isBlank()) {
			return null;
		}
		try {
			return StoragePlatformEnum.valueOf(platform.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Unsupported storage platform: " + platform, exception);
		}
	}

}
