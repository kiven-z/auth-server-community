package com.auth.service.system.file.config;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import com.auth.service.system.file.storage.platform.aliyun.AliyunOssConfigValidator;
import com.auth.service.system.file.storage.platform.minio.MinioConfigValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * S3 兼容存储 Provider Bean 配置
 *
 * <p>
 * {@link S3CompatibleFileStorageProvider} 需要绑定具体平台后才有意义，因此以工厂方式为每个平台产出独立实例， 并按平台命名 Bean
 * 供各自 Facade 通过 @Qualifier 装配。
 * </p>
 *
 * @author Bunny
 */
@Configuration
public class S3StorageProviderConfiguration {

	/**
	 * MinIO 平台使用的 S3 Provider
	 * @param profileResolver S3 配置解析器
	 * @param clientManager S3 客户端管理器
	 * @param configValidator MinIO 校验器
	 * @param exceptionClassifier S3 异常分类器
	 * @return 绑定到 {@link StoragePlatformEnum#MINIO} 的 Provider
	 */
	@Bean("minioFileStorageProvider")
	public S3CompatibleFileStorageProvider minioFileStorageProvider(S3PlatformProfileResolver profileResolver,
			S3ClientManager clientManager, MinioConfigValidator configValidator,
			StorageExceptionClassifier exceptionClassifier) {
		return new S3CompatibleFileStorageProvider(StoragePlatformEnum.MINIO, profileResolver, clientManager,
				configValidator, exceptionClassifier);
	}

	/**
	 * 阿里云 OSS 平台使用的 S3 Provider
	 * @param profileResolver S3 配置解析器
	 * @param clientManager S3 客户端管理器
	 * @param configValidator OSS 校验器
	 * @param exceptionClassifier S3 异常分类器
	 * @return 绑定到 {@link StoragePlatformEnum#ALIYUN_OSS} 的 Provider
	 */
	@Bean("aliyunOssFileStorageProvider")
	public S3CompatibleFileStorageProvider aliyunOssFileStorageProvider(S3PlatformProfileResolver profileResolver,
			S3ClientManager clientManager, AliyunOssConfigValidator configValidator,
			StorageExceptionClassifier exceptionClassifier) {
		return new S3CompatibleFileStorageProvider(StoragePlatformEnum.ALIYUN_OSS, profileResolver, clientManager,
				configValidator, exceptionClassifier);
	}

}
