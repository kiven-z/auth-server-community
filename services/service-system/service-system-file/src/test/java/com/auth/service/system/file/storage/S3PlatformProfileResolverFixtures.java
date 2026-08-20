package com.auth.service.system.file.storage;

import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import com.auth.service.system.file.storage.platform.aliyun.AliyunOssS3PlatformProfileSource;
import com.auth.service.system.file.storage.platform.minio.MinioS3PlatformProfileSource;

import java.util.List;

/**
 * {@link S3PlatformProfileResolver} 测试装配辅助
 *
 * @author Bunny
 */
public final class S3PlatformProfileResolverFixtures {

	private S3PlatformProfileResolverFixtures() {
	}

	/**
	 * 构造带默认平台来源的解析器
	 * @param properties 文件上传配置
	 * @return S3 协议配置解析器
	 */
	public static S3PlatformProfileResolver defaultResolver(FileUploadProperties properties) {
		return new S3PlatformProfileResolver(properties,
				List.of(new MinioS3PlatformProfileSource(), new AliyunOssS3PlatformProfileSource()));
	}

}
