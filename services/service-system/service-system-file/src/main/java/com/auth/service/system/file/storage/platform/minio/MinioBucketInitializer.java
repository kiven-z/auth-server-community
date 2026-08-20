package com.auth.service.system.file.storage.platform.minio;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.core.bucket.AbstractS3CompatibleBucketInitializer;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

/**
 * MinIO 存储桶初始化器
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MinioBucketInitializer extends AbstractS3CompatibleBucketInitializer {

	/**
	 * MinIO public 前缀策略模板路径
	 */
	public static final String POLICY_TEMPLATE_PATH = "storage/policies/minio-public-prefix-policy.json";

	/**
	 * 存储桶占位符
	 */
	public static final String BUCKET_PLACEHOLDER = "${bucket}";

	private static final StoragePlatformEnum STORAGE_PLATFORM = StoragePlatformEnum.MINIO;

	/**
	 * 构造 MinIO 存储桶初始化器
	 * @param profileResolver 平台配置解析器
	 * @param s3ClientManager S3 客户端管理器
	 * @param exceptionClassifier 存储异常分类器
	 */
	public MinioBucketInitializer(S3PlatformProfileResolver profileResolver, S3ClientManager s3ClientManager,
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void afterBucketCreated(S3Client client, String bucket) {
		String template = ResourceUtil.readUtf8Str(POLICY_TEMPLATE_PATH);
		String policyJson = CharSequenceUtil.replace(template, BUCKET_PLACEHOLDER, bucket);
		client.putBucketPolicy(PutBucketPolicyRequest.builder().bucket(bucket).policy(policyJson).build());
		log.info("Applied MinIO public prefix policy after bucket creation: bucket={}", bucket);
	}

}
