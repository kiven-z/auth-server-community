package com.auth.service.system.file.storage.platform.minio;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.core.AbstractS3CompatibleStoragePlatformFacade;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * MinIO 平台门面
 *
 * @author Bunny
 */
@Component
public class MinioStoragePlatformFacade extends AbstractS3CompatibleStoragePlatformFacade {

	/**
	 * 构造 MinIO 平台门面
	 * @param fileStorageProvider 文件存储能力
	 * @param configValidator 平台配置校验能力
	 * @param bucketInitializer 存储桶初始化能力
	 * @param exceptionClassifier 存储异常分类器
	 */
	public MinioStoragePlatformFacade(
			@Qualifier("minioFileStorageProvider") S3CompatibleFileStorageProvider fileStorageProvider,
			MinioConfigValidator configValidator, MinioBucketInitializer bucketInitializer,
			StorageExceptionClassifier exceptionClassifier) {
		super(fileStorageProvider, bucketInitializer, configValidator, exceptionClassifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoragePlatformEnum platform() {
		return StoragePlatformEnum.MINIO;
	}

}
