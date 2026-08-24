package com.auth.service.system.file.storage;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link S3PlatformProfileResolver} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("S3PlatformProfileResolver 配置路由")
class S3PlatformProfileResolverTest {

	@Test
	@DisplayName("resolve：返回 platforms 显式配置")
	void resolveReturnsExplicitPlatformProfile() {
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfile explicitProfile = new S3PlatformProfile();
		explicitProfile.setEndpoint("http://explicit:9000");
		explicitProfile.setRegion("eu-west-1");
		explicitProfile.setAccessKey("ak");
		explicitProfile.setSecretKey("sk");
		explicitProfile.setBucket("bucket-a");
		properties.getPlatforms().put(StoragePlatformEnum.MINIO, explicitProfile);
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);

		S3PlatformProfile profile = resolver.resolve(StoragePlatformEnum.MINIO);

		assertThat(profile.getEndpoint()).isEqualTo("http://explicit:9000");
		assertThat(profile.getRegion()).isEqualTo("eu-west-1");
	}

	@Test
	@DisplayName("resolve：无协议 endpoint 补全为 https 绝对地址")
	void resolveNormalizesSchemelessEndpoint() {
		// 控制台复制的 OSS endpoint 常无协议，解析出口必须兑现绝对 URL 契约。
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.resolverWithAliyunOss(properties,
				"oss-cn-shanghai.aliyuncs.com", "bunny-auth");

		S3PlatformProfile profile = resolver.resolve(StoragePlatformEnum.ALIYUN_OSS);

		assertThat(profile.getEndpoint()).isEqualTo("https://oss-cn-shanghai.aliyuncs.com");
		assertThat(profile.getRegion()).isEqualTo("cn-shanghai");
		assertThat(profile.isPathStyleAccess()).isFalse();
	}

	@Test
	@DisplayName("resolve：返回副本，不改写 platforms 绑定实例")
	void resolveReturnsCopyWithoutMutatingBoundProfile() {
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfile bound = new S3PlatformProfile();
		bound.setEndpoint("oss-cn-shanghai.aliyuncs.com");
		bound.setRegion("cn-shanghai");
		bound.setAccessKey("ak");
		bound.setSecretKey("sk");
		bound.setBucket("bunny-auth");
		bound.setPathStyleAccess(false);
		properties.getPlatforms().put(StoragePlatformEnum.ALIYUN_OSS, bound);
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);

		S3PlatformProfile resolved = resolver.resolve(StoragePlatformEnum.ALIYUN_OSS);

		assertThat(resolved.getEndpoint()).isEqualTo("https://oss-cn-shanghai.aliyuncs.com");
		assertThat(bound.getEndpoint()).isEqualTo("oss-cn-shanghai.aliyuncs.com");
		assertThat(resolved).isNotSameAs(bound);
	}

	@Test
	@DisplayName("resolve：平台未配置时抛出配置缺失异常")
	void resolveThrowsWhenPlatformConfigMissing() {
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);

		assertThatThrownBy(() -> resolver.resolve(StoragePlatformEnum.ALIYUN_OSS))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING);
	}

}
