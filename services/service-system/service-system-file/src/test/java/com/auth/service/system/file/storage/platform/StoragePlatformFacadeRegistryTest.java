package com.auth.service.system.file.storage.platform;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.bucket.StorageBucketInitializer;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link StoragePlatformFacadeRegistry} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("StoragePlatformFacadeRegistry 门面路由")
class StoragePlatformFacadeRegistryTest {

	@Test
	@DisplayName("resolve：返回匹配平台的 facade")
	void resolveReturnsMatchedFacade() {
		// 验证注册表能够按平台返回对应门面实现。
		StoragePlatformFacade minioFacade = new StubFacade(StoragePlatformEnum.MINIO);
		StoragePlatformFacade aliyunFacade = new StubFacade(StoragePlatformEnum.ALIYUN_OSS);
		StoragePlatformFacadeRegistry registry = new StoragePlatformFacadeRegistry(List.of(minioFacade, aliyunFacade));

		assertThat(registry.resolve(StoragePlatformEnum.MINIO)).isSameAs(minioFacade);
		assertThat(registry.resolve(StoragePlatformEnum.ALIYUN_OSS)).isSameAs(aliyunFacade);
	}

	@Test
	@DisplayName("resolve：平台未注册时抛出业务异常")
	void resolveThrowsWhenFacadeMissing() {
		// 验证未注册平台会抛统一业务异常码，避免门面为空。
		StoragePlatformFacadeRegistry registry = new StoragePlatformFacadeRegistry(
				List.of(new StubFacade(StoragePlatformEnum.MINIO)));

		assertThatThrownBy(() -> registry.resolve(StoragePlatformEnum.ALIYUN_OSS))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED);
	}

	@Test
	@DisplayName("resolve：平台为空时抛出配置缺失异常")
	void resolveThrowsWhenPlatformMissing() {
		// 验证未配置默认平台时返回统一配置缺失错误。
		StoragePlatformFacadeRegistry registry = new StoragePlatformFacadeRegistry(
				List.of(new StubFacade(StoragePlatformEnum.MINIO)));

		assertThatThrownBy(() -> registry.resolve(null)).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING);
	}

	@Test
	@DisplayName("registry：同平台重复注册时抛出业务异常")
	void constructorThrowsWhenPlatformDuplicated() {
		// 验证同一平台出现多个门面实现时，构造阶段立刻失败。
		StoragePlatformFacade firstFacade = new StubFacade(StoragePlatformEnum.MINIO);
		StoragePlatformFacade secondFacade = new StubFacade(StoragePlatformEnum.MINIO);

		List<StoragePlatformFacade> facades = List.of(firstFacade, secondFacade);
		assertThatThrownBy(() -> new StoragePlatformFacadeRegistry(facades)).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_PLATFORM_DUPLICATED);
	}

	/**
	 * 测试桩平台门面。
	 */
	private record StubFacade(StoragePlatformEnum platform) implements StoragePlatformFacade {

		@Override
		public FileStorageProvider provider() {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public StorageBucketInitializer initializer() {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public StoragePlatformConfigValidator validator() {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public StorageExceptionClassifier exceptionClassifier() {
			throw new UnsupportedOperationException("not used");
		}
	}

}
