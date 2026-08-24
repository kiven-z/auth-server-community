package com.auth.service.system.file.storage.core.s3;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.utils.StorageUrlUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * S3 协议配置解析器：仅读取 {@code auth.file.platforms}
 *
 * @author Bunny
 */
@Component
public class S3PlatformProfileResolver {

	private final FileUploadProperties fileUploadProperties;

	public S3PlatformProfileResolver(FileUploadProperties fileUploadProperties) {
		this.fileUploadProperties = fileUploadProperties;
	}

	/**
	 * 依据平台返回运行时 S3 协议配置（独立副本，endpoint 为绝对 http(s) URL）
	 * @param platform 存储平台
	 * @return S3 协议配置
	 */
	public S3PlatformProfile resolve(StoragePlatformEnum platform) {
		S3PlatformProfile raw = resolveRaw(platform);
		S3PlatformProfile profile = raw.copy();
		if (CharSequenceUtil.isBlank(profile.getEndpoint())) {
			return profile;
		}
		profile.setEndpoint(StorageUrlUtil.normalizeHttpBaseUrl(profile.getEndpoint()));
		return profile;
	}

	/**
	 * 解析原始平台配置
	 * @param platform 存储平台
	 * @return 未经规范化的配置（可能是 Spring 绑定实例，勿直接改写）
	 */
	private S3PlatformProfile resolveRaw(StoragePlatformEnum platform) {
		Map<StoragePlatformEnum, S3PlatformProfile> platforms = fileUploadProperties.getPlatforms();
		S3PlatformProfile profile = platforms == null ? null : platforms.get(platform);
		if (profile == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING, platform.name());
		}
		return profile;
	}

}
