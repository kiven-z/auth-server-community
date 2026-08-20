package com.auth.service.system.file.storage.core.validator;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.file.exception.FileStorageException;

import static com.auth.service.system.file.exception.FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING;

/**
 * 存储平台配置校验器
 *
 * @author Bunny
 */
public interface StoragePlatformConfigValidator {

	/**
	 * 断言配置项非空
	 * @param value 配置值
	 * @param configKey 配置键
	 */
	default void assertNotBlank(String value, String configKey) {
		if (CharSequenceUtil.isBlank(value)) {
			throw new FileStorageException(FILE_STORAGE_CONFIG_MISSING, configKey);
		}
	}

	/**
	 * 当前平台配置是否完整
	 * @return true=配置完整
	 */
	default boolean isConfigured() {
		try {
			validateOrThrow();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * 断言当前平台配置完整，不满足时抛出业务异常
	 */
	void validateOrThrow();

}
