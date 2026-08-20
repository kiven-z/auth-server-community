package com.auth.service.system.file.storage.platform.aliyun;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AliyunOssS3PlatformProfileSource} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("AliyunOssS3PlatformProfileSource 阿里云配置适配")
class AliyunOssS3PlatformProfileSourceTest {

	@Test
	@DisplayName("resolveFallback：映射 aliyunOss 配置并推断 region")
	void resolveFallbackMapsAliyunPropertiesAndDerivesRegion() {
		// 验证阿里云 legacy 配置正确映射，并从 endpoint 推断 region。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getAliyunOss().setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
		properties.getAliyunOss().setAccessKeyId("ak-id");
		properties.getAliyunOss().setAccessKeySecret("ak-secret");
		properties.getAliyunOss().setBucket("bucket-b");
		AliyunOssS3PlatformProfileSource source = new AliyunOssS3PlatformProfileSource();

		S3PlatformProfile profile = source.resolveFallback(properties);

		assertThat(source.platform()).isEqualTo(StoragePlatformEnum.ALIYUN_OSS);
		assertThat(profile.getEndpoint()).isEqualTo("https://oss-cn-hangzhou.aliyuncs.com");
		assertThat(profile.getRegion()).isEqualTo("oss-cn-hangzhou");
		assertThat(profile.getAccessKey()).isEqualTo("ak-id");
		assertThat(profile.getSecretKey()).isEqualTo("ak-secret");
		assertThat(profile.getBucket()).isEqualTo("bucket-b");
		assertThat(profile.isPathStyleAccess()).isFalse();
	}

	@Test
	@DisplayName("resolveFallback：endpoint 为空时使用兜底 region")
	void resolveFallbackUsesFallbackRegionWhenEndpointBlank() {
		// 验证 endpoint 缺失时仍返回 S3 SDK 可用的 region。
		FileUploadProperties properties = new FileUploadProperties();
		AliyunOssS3PlatformProfileSource source = new AliyunOssS3PlatformProfileSource();

		S3PlatformProfile profile = source.resolveFallback(properties);

		assertThat(profile.getRegion()).isEqualTo("oss-cn-hangzhou");
	}

}
