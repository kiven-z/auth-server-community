package com.auth.service.system.file.storage.core;

import com.auth.service.system.file.storage.core.bucket.StorageBucketInitializer;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;

/**
 * S3 兼容存储平台门面模板，聚合同类平台的通用能力委托。
 *
 * @author Bunny
 */
public abstract class AbstractS3CompatibleStoragePlatformFacade implements StoragePlatformFacade {

	private final S3CompatibleFileStorageProvider fileStorageProvider;

	private final StorageBucketInitializer bucketInitializer;

	private final StoragePlatformConfigValidator configValidator;

	private final StorageExceptionClassifier exceptionClassifier;

	/**
	 * 构造 S3 兼容平台门面
	 * @param fileStorageProvider 文件存储能力
	 * @param bucketInitializer 存储桶初始化能力
	 * @param configValidator 平台配置校验能力
	 * @param exceptionClassifier 存储异常分类器
	 */
	protected AbstractS3CompatibleStoragePlatformFacade(S3CompatibleFileStorageProvider fileStorageProvider,
			StorageBucketInitializer bucketInitializer, StoragePlatformConfigValidator configValidator,
			StorageExceptionClassifier exceptionClassifier) {
		this.fileStorageProvider = fileStorageProvider;
		this.bucketInitializer = bucketInitializer;
		this.configValidator = configValidator;
		this.exceptionClassifier = exceptionClassifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileStorageProvider provider() {
		return fileStorageProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StorageBucketInitializer initializer() {
		return bucketInitializer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoragePlatformConfigValidator validator() {
		return configValidator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StorageExceptionClassifier exceptionClassifier() {
		return exceptionClassifier;
	}

}
