package com.auth.service.system.file.storage;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;

/**
 * {@link S3PlatformProfileResolver} 测试装配辅助
 *
 * @author Bunny
 */
public final class S3PlatformProfileResolverFixtures {

	private S3PlatformProfileResolverFixtures() {
	}

	/**
	 * 构造仅读 platforms 的解析器
	 * @param properties 文件上传配置
	 * @return S3 协议配置解析器
	 */
	public static S3PlatformProfileResolver defaultResolver(FileUploadProperties properties) {
		return new S3PlatformProfileResolver(properties);
	}

	/**
	 * 写入 MinIO 平台配置并返回解析器
	 * @param properties 文件上传配置
	 * @param endpoint 端点
	 * @param bucket 桶名
	 * @return 解析器
	 */
	public static S3PlatformProfileResolver resolverWithMinio(FileUploadProperties properties, String endpoint,
			String bucket) {
		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint(endpoint);
		profile.setRegion("us-east-1");
		profile.setAccessKey("ak");
		profile.setSecretKey("sk");
		profile.setBucket(bucket);
		profile.setPathStyleAccess(true);
		properties.getPlatforms().put(StoragePlatformEnum.MINIO, profile);
		return defaultResolver(properties);
	}

	/**
	 * 写入阿里云 OSS 平台配置并返回解析器
	 * @param properties 文件上传配置
	 * @param endpoint 端点
	 * @param bucket 桶名
	 * @return 解析器
	 */
	public static S3PlatformProfileResolver resolverWithAliyunOss(FileUploadProperties properties, String endpoint,
			String bucket) {
		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint(endpoint);
		profile.setRegion("cn-shanghai");
		profile.setAccessKey("ak");
		profile.setSecretKey("sk");
		profile.setBucket(bucket);
		profile.setPathStyleAccess(false);
		properties.getPlatforms().put(StoragePlatformEnum.ALIYUN_OSS, profile);
		return defaultResolver(properties);
	}

}
