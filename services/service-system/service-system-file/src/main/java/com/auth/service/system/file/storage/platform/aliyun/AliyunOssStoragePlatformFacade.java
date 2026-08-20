package com.auth.service.system.file.storage.platform.aliyun;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.core.AbstractS3CompatibleStoragePlatformFacade;
import com.auth.service.system.file.storage.core.capability.ImageStyleUrlCapability;
import com.auth.service.system.file.storage.core.capability.StoragePlatformCapability;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 阿里云 OSS 平台门面
 *
 * @author Bunny
 */
@Component
public class AliyunOssStoragePlatformFacade extends AbstractS3CompatibleStoragePlatformFacade {

	private final AliyunImageStyleUrlCapability imageStyleUrlCapability;

	/**
	 * 构造阿里云 OSS 平台门面
	 * @param fileStorageProvider 文件存储能力
	 * @param configValidator 平台配置校验能力
	 * @param bucketInitializer 存储桶初始化能力
	 * @param exceptionClassifier 存储异常分类器
	 * @param imageStyleUrlCapability 图片样式 URL 能力
	 */
	public AliyunOssStoragePlatformFacade(
			@Qualifier("aliyunOssFileStorageProvider") S3CompatibleFileStorageProvider fileStorageProvider,
			AliyunOssConfigValidator configValidator, AliyunOssBucketInitializer bucketInitializer,
			StorageExceptionClassifier exceptionClassifier, AliyunImageStyleUrlCapability imageStyleUrlCapability) {
		super(fileStorageProvider, bucketInitializer, configValidator, exceptionClassifier);
		this.imageStyleUrlCapability = imageStyleUrlCapability;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoragePlatformEnum platform() {
		return StoragePlatformEnum.ALIYUN_OSS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends StoragePlatformCapability> Optional<T> capability(Class<T> capabilityType) {
		if (capabilityType == ImageStyleUrlCapability.class) {
			return Optional.of(capabilityType.cast(imageStyleUrlCapability));
		}
		return Optional.empty();
	}

}
