package com.auth.service.system.file.storage.platform.aliyun;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 平台配置校验器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class AliyunOssConfigValidator implements StoragePlatformConfigValidator {

	private static final String CONFIG_PREFIX = "auth.file.platforms.ALIYUN_OSS";

	private final S3PlatformProfileResolver profileResolver;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateOrThrow() {
		S3PlatformProfile profile = profileResolver.resolve(StoragePlatformEnum.ALIYUN_OSS);
		assertNotBlank(profile.getEndpoint(), CONFIG_PREFIX + ".endpoint");
		assertNotBlank(profile.getRegion(), CONFIG_PREFIX + ".region");
		assertNotBlank(profile.getAccessKey(), CONFIG_PREFIX + ".access-key");
		assertNotBlank(profile.getSecretKey(), CONFIG_PREFIX + ".secret-key");
		assertNotBlank(profile.getBucket(), CONFIG_PREFIX + ".bucket");
	}

}
