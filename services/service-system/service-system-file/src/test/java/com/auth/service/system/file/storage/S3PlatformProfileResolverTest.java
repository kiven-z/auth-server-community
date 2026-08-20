package com.auth.service.system.file.storage;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileSource;
import com.auth.service.system.file.storage.platform.minio.MinioS3PlatformProfileSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
	@DisplayName("resolve：优先返回 platforms 显式配置")
	void resolvePrefersExplicitPlatformProfile() {
		// 验证显式 platforms 配置覆盖 legacy 适配结果。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getMinio().setEndpoint("http://legacy:9000");
		S3PlatformProfile explicitProfile = new S3PlatformProfile();
		explicitProfile.setEndpoint("http://explicit:9000");
		explicitProfile.setRegion("eu-west-1");
		properties.getPlatforms().put(StoragePlatformEnum.MINIO, explicitProfile);
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);

		S3PlatformProfile profile = resolver.resolve(StoragePlatformEnum.MINIO);

		assertThat(profile.getEndpoint()).isEqualTo("http://explicit:9000");
		assertThat(profile.getRegion()).isEqualTo("eu-west-1");
	}

	@Test
	@DisplayName("resolve：无显式配置时委托平台来源适配")
	void resolveDelegatesToPlatformSource() {
		// 验证缺省 platforms 时走 MinIO 来源适配。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getMinio().setEndpoint("http://127.0.0.1:9000");
		properties.getMinio().setAccessKey("ak");
		properties.getMinio().setSecretKey("sk");
		properties.getMinio().setBucket("bucket-a");
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);

		S3PlatformProfile profile = resolver.resolve(StoragePlatformEnum.MINIO);

		assertThat(profile.getEndpoint()).isEqualTo("http://127.0.0.1:9000");
		assertThat(profile.getRegion()).isEqualTo("us-east-1");
		assertThat(profile.isPathStyleAccess()).isTrue();
	}

	@Test
	@DisplayName("resolve：无协议 endpoint 补全为 https 绝对地址")
	void resolveNormalizesSchemelessEndpoint() {
		// 控制台复制的 OSS endpoint 常无协议，解析出口必须兑现绝对 URL 契约。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getAliyunOss().setEndpoint("oss-cn-shanghai.aliyuncs.com");
		properties.getAliyunOss().setAccessKeyId("ak-id");
		properties.getAliyunOss().setAccessKeySecret("ak-secret");
		properties.getAliyunOss().setBucket("bunny-auth");
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);

		S3PlatformProfile profile = resolver.resolve(StoragePlatformEnum.ALIYUN_OSS);

		assertThat(profile.getEndpoint()).isEqualTo("https://oss-cn-shanghai.aliyuncs.com");
		assertThat(profile.getRegion()).isEqualTo("oss-cn-shanghai");
	}

	@Test
	@DisplayName("resolve：返回副本，不改写 platforms 绑定实例")
	void resolveReturnsCopyWithoutMutatingBoundProfile() {
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfile bound = new S3PlatformProfile();
		bound.setEndpoint("oss-cn-shanghai.aliyuncs.com");
		bound.setRegion("oss-cn-shanghai");
		bound.setAccessKey("ak");
		bound.setSecretKey("sk");
		bound.setBucket("bunny-auth");
		properties.getPlatforms().put(StoragePlatformEnum.ALIYUN_OSS, bound);
		S3PlatformProfileResolver resolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);

		S3PlatformProfile resolved = resolver.resolve(StoragePlatformEnum.ALIYUN_OSS);

		assertThat(resolved.getEndpoint()).isEqualTo("https://oss-cn-shanghai.aliyuncs.com");
		assertThat(bound.getEndpoint()).isEqualTo("oss-cn-shanghai.aliyuncs.com");
		assertThat(resolved).isNotSameAs(bound);
	}

	@Test
	@DisplayName("resolve：平台未注册时抛出业务异常")
	void resolveThrowsWhenPlatformUnsupported() {
		// 验证未注册平台会抛统一业务异常码。
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfileResolver resolver = new S3PlatformProfileResolver(properties,
				List.of(new MinioS3PlatformProfileSource()));

		assertThatThrownBy(() -> resolver.resolve(StoragePlatformEnum.ALIYUN_OSS))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED);
	}

	@Test
	@DisplayName("constructor：同平台重复注册时抛出业务异常")
	void constructorThrowsWhenPlatformDuplicated() {
		// 验证同一平台出现多个来源实现时，构造阶段立刻失败。
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfileSource firstSource = new MinioS3PlatformProfileSource();
		S3PlatformProfileSource secondSource = new MinioS3PlatformProfileSource();

		List<S3PlatformProfileSource> sources = List.of(firstSource, secondSource);
		assertThatThrownBy(() -> new S3PlatformProfileResolver(properties, sources))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_PLATFORM_DUPLICATED);
	}

}
