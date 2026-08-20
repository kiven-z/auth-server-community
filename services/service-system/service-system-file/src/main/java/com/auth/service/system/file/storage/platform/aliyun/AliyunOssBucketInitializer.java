package com.auth.service.system.file.storage.platform.aliyun;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.core.bucket.AbstractS3CompatibleBucketInitializer;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 存储桶初始化器
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AliyunOssBucketInitializer extends AbstractS3CompatibleBucketInitializer {

	private static final StoragePlatformEnum STORAGE_PLATFORM = StoragePlatformEnum.ALIYUN_OSS;

	/**
	 * 构造阿里云 OSS 存储桶初始化器
	 * @param profileResolver 平台配置解析器
	 * @param s3ClientManager S3 客户端管理器
	 * @param exceptionClassifier 存储异常分类器
	 */
	public AliyunOssBucketInitializer(S3PlatformProfileResolver profileResolver, S3ClientManager s3ClientManager,
			StorageExceptionClassifier exceptionClassifier) {
		super(profileResolver, s3ClientManager, exceptionClassifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StoragePlatformEnum platform() {
		return STORAGE_PLATFORM;
	}

}
