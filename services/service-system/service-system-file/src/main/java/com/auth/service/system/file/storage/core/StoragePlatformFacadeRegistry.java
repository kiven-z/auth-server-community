package com.auth.service.system.file.storage.core;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 存储平台门面注册表
 *
 * @author Bunny
 */
@Component
public class StoragePlatformFacadeRegistry {

	private final Map<StoragePlatformEnum, StoragePlatformFacade> facadeByPlatform;

	public StoragePlatformFacadeRegistry(List<StoragePlatformFacade> facades) {
		Map<StoragePlatformEnum, StoragePlatformFacade> mutableFacadeByPlatform = new EnumMap<>(
				StoragePlatformEnum.class);
		for (StoragePlatformFacade facade : facades) {
			StoragePlatformEnum platform = facade.platform();
			StoragePlatformFacade previousFacade = mutableFacadeByPlatform.put(platform, facade);
			if (previousFacade != null) {
				throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_PLATFORM_DUPLICATED, platform.name());
			}
		}
		this.facadeByPlatform = Collections.unmodifiableMap(mutableFacadeByPlatform);
	}

	/**
	 * 按平台解析门面
	 * @param platform 存储平台
	 * @return 平台门面
	 */
	public StoragePlatformFacade resolve(StoragePlatformEnum platform) {
		if (platform == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING,
					"auth.file.default-platform");
		}

		StoragePlatformFacade facade = facadeByPlatform.get(platform);
		if (facade == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED, platform.name());
		}
		return facade;
	}

}
