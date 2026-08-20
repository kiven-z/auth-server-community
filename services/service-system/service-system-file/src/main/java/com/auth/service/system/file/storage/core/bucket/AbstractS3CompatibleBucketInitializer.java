package com.auth.service.system.file.storage.core.bucket;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * S3 兼容存储桶初始化模板
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractS3CompatibleBucketInitializer implements StorageBucketInitializer {

	private final S3PlatformProfileResolver profileResolver;

	private final S3ClientManager s3ClientManager;

	private final StorageExceptionClassifier exceptionClassifier;

	/**
	 * 当前初始化器对应的存储平台
	 * @return 存储平台枚举
	 */
	protected abstract StoragePlatformEnum platform();

	/**
	 * 存储桶已存在时的日志输出
	 * @param bucket 存储桶名称
	 */
	protected void logBucketAlreadyExists(String bucket) {
		log.info("{} bucket already exists: bucket={}", platform().name(), bucket);
	}

	/**
	 * 存储桶创建成功后的日志输出
	 * @param bucket 存储桶名称
	 */
	protected void logBucketCreated(String bucket) {
		log.info("Created {} bucket: bucket={}", platform().name(), bucket);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void ensureBucketReady() {
		S3PlatformProfile profile = profileResolver.resolve(platform());
		String bucket = profile.getBucket();
		S3Client client = s3ClientManager.getClient(platform());
		try {
			if (bucketExists(client, bucket)) {
				logBucketAlreadyExists(bucket);
				return;
			}
			client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
			logBucketCreated(bucket);
			afterBucketCreated(client, bucket);
		}
		catch (Exception exception) {
			throw new FileStorageException(FileUploadResultCode.FILE_UPLOAD_FAILED, exception, platform().name(),
					exception.getMessage());
		}
	}

	/**
	 * 存储桶创建完成后的扩展点，默认无操作
	 * @param client S3 客户端
	 * @param bucket 存储桶名称
	 */
	protected void afterBucketCreated(S3Client client, String bucket) {
		// 默认无后置逻辑
	}

	private boolean bucketExists(S3Client client, String bucket) {
		try {
			client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
			return true;
		}
		catch (Exception exception) {
			if (exceptionClassifier.isNotFound(exception)) {
				return false;
			}
			throw exception;
		}
	}

}
