package com.auth.service.system.file.storage.platform;

import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import com.auth.service.system.file.storage.platform.aliyun.AliyunImageStyleUrlCapability;
import com.auth.service.system.file.storage.platform.aliyun.AliyunOssBucketInitializer;
import com.auth.service.system.file.storage.platform.aliyun.AliyunOssConfigValidator;
import com.auth.service.system.file.storage.platform.aliyun.AliyunOssStoragePlatformFacade;
import com.auth.service.system.file.storage.platform.minio.MinioBucketInitializer;
import com.auth.service.system.file.storage.platform.minio.MinioConfigValidator;
import com.auth.service.system.file.storage.platform.minio.MinioStoragePlatformFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 平台门面与拆分后能力组件的绑定关系测试。
 *
 * @author Bunny
 */
@DisplayName("StoragePlatformFacade 能力绑定")
class StoragePlatformFacadeSupportBindingTest {

	@Test
	@DisplayName("MinIO facade：所有能力字段均绑定到独立组件")
	void minioFacadeBindsDistinctComponents() {
		// 验证 MinIO 门面 provider/validator/initializer/exceptionClassifier 均由 Spring 独立注入。
		S3CompatibleFileStorageProvider provider = mock(S3CompatibleFileStorageProvider.class);
		MinioConfigValidator configValidator = mock(MinioConfigValidator.class);
		MinioBucketInitializer bucketInitializer = mock(MinioBucketInitializer.class);
		StorageExceptionClassifier exceptionClassifier = mock(StorageExceptionClassifier.class);
		MinioStoragePlatformFacade facade = new MinioStoragePlatformFacade(provider, configValidator, bucketInitializer,
				exceptionClassifier);

		assertThat(facade.validator()).isSameAs(configValidator);
		assertThat(facade.initializer()).isSameAs(bucketInitializer);
		assertThat(facade.provider()).isSameAs(provider);
		assertThat(facade.exceptionClassifier()).isSameAs(exceptionClassifier);
	}

	@Test
	@DisplayName("Aliyun facade：所有能力字段均绑定到独立组件")
	void aliyunFacadeBindsDistinctComponents() {
		// 验证阿里云门面 provider/validator/initializer/exceptionClassifier/capability 均由 Spring
		// 独立注入。
		S3CompatibleFileStorageProvider provider = mock(S3CompatibleFileStorageProvider.class);
		AliyunOssConfigValidator configValidator = mock(AliyunOssConfigValidator.class);
		AliyunOssBucketInitializer bucketInitializer = mock(AliyunOssBucketInitializer.class);
		StorageExceptionClassifier exceptionClassifier = mock(StorageExceptionClassifier.class);
		AliyunImageStyleUrlCapability imageStyleUrlCapability = mock(AliyunImageStyleUrlCapability.class);
		AliyunOssStoragePlatformFacade facade = new AliyunOssStoragePlatformFacade(provider, configValidator,
				bucketInitializer, exceptionClassifier, imageStyleUrlCapability);

		assertThat(facade.validator()).isSameAs(configValidator);
		assertThat(facade.initializer()).isSameAs(bucketInitializer);
		assertThat(facade.provider()).isSameAs(provider);
		assertThat(facade.exceptionClassifier()).isSameAs(exceptionClassifier);
	}

}
