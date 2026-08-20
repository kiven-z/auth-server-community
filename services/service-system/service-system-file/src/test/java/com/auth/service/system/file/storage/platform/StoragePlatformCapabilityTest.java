package com.auth.service.system.file.storage.platform;

import com.auth.service.system.file.storage.core.capability.ImageStyleUrlCapability;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 平台 Capability SPI 行为差异测试。
 *
 * @author Bunny
 */
@DisplayName("StoragePlatformFacade Capability SPI")
class StoragePlatformCapabilityTest {

	@Test
	@DisplayName("MinIO facade：未实现的能力返回 Optional.empty")
	void minioCapability_returnsEmptyForUnimplementedType() {
		// 验证 MinIO 不实现图片样式能力时通过默认方法返回空。
		MinioStoragePlatformFacade facade = new MinioStoragePlatformFacade(mock(S3CompatibleFileStorageProvider.class),
				mock(MinioConfigValidator.class), mock(MinioBucketInitializer.class),
				mock(StorageExceptionClassifier.class));

		Optional<ImageStyleUrlCapability> capability = facade.capability(ImageStyleUrlCapability.class);

		assertThat(capability).isEmpty();
	}

	@Test
	@DisplayName("Aliyun facade：图片样式能力可用且能拼出 x-oss-process 参数")
	void aliyunCapability_returnsImageStyleUrlBuilder() {
		// 验证 OSS 侧实现按预期拼接样式参数，覆盖有/无查询串两种情形。
		AliyunImageStyleUrlCapability imageStyle = new AliyunImageStyleUrlCapability();
		AliyunOssStoragePlatformFacade facade = new AliyunOssStoragePlatformFacade(
				mock(S3CompatibleFileStorageProvider.class), mock(AliyunOssConfigValidator.class),
				mock(AliyunOssBucketInitializer.class), mock(StorageExceptionClassifier.class), imageStyle);

		Optional<ImageStyleUrlCapability> capability = facade.capability(ImageStyleUrlCapability.class);

		assertThat(capability).isPresent();
		assertThat(capability.get().buildStyleUrl("https://cdn.example.com/a.png", "thumb"))
			.isEqualTo("https://cdn.example.com/a.png?x-oss-process=style/thumb");
		assertThat(capability.get().buildStyleUrl("https://cdn.example.com/a.png?v=1", "thumb"))
			.isEqualTo("https://cdn.example.com/a.png?v=1&x-oss-process=style/thumb");
	}

}
