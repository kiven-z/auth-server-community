package com.auth.service.system.file.storage.platform.minio;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.config.properties.platform.MinioStorageProperties;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileSource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * MinIO 平台 S3 协议配置来源
 *
 * @author Bunny
 */
@Component
public class MinioS3PlatformProfileSource implements S3PlatformProfileSource {

	/**
	 * MinIO 走 S3 兼容协议时的兜底 region
	 */
	private static final String MINIO_S3_FALLBACK_REGION = "us-east-1";

	@Override
	public StoragePlatformEnum platform() {
		return StoragePlatformEnum.MINIO;
	}

	@Override
	public S3PlatformProfile resolveFallback(FileUploadProperties properties) {
		MinioStorageProperties minio = Objects.requireNonNullElseGet(properties.getMinio(),
				MinioStorageProperties::new);

		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint(minio.getEndpoint());
		profile.setRegion(MINIO_S3_FALLBACK_REGION);
		profile.setAccessKey(minio.getAccessKey());
		profile.setSecretKey(minio.getSecretKey());
		profile.setBucket(minio.getBucket());
		profile.setPublicUrl(minio.getPublicUrl());
		return profile;
	}

}
