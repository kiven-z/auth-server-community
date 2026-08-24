package com.auth.service.system.file.storage.core.provider;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.S3PlatformProfileResolverFixtures;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link S3ClientManager} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("S3ClientManager S3 客户端管理")
class S3ClientManagerTest {

	private static final String MINIO_ENDPOINT = "http://127.0.0.1:9000";

	private static final String ACCESS_KEY = "test-access-key";

	private static final String SECRET_KEY = "test-secret-key";

	private static final String BUCKET = "test-bucket";

	@Test
	@DisplayName("getClient：缺少必填字段时抛出配置缺失异常")
	void getClient_throwsWhenProfileIncomplete() {
		// 验证懒加载时会校验必填项，避免运行时才在 SDK 层暴露。
		FileUploadProperties properties = new FileUploadProperties();
		S3ClientManager manager = new S3ClientManager(S3PlatformProfileResolverFixtures.defaultResolver(properties));

		assertThatThrownBy(() -> manager.getClient(StoragePlatformEnum.MINIO)).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING);
	}

	@Test
	@DisplayName("getClient：懒加载单例复用，同平台多次调用返回同一实例")
	void getClient_lazyInitSingletonPerPlatform() {
		// 验证客户端只在首次访问时构建，之后复用同一实例。
		S3ClientManager manager = buildConfiguredManager();

		S3Client first = manager.getClient(StoragePlatformEnum.MINIO);
		S3Client second = manager.getClient(StoragePlatformEnum.MINIO);

		assertThat(first).isSameAs(second);
		manager.shutdown();
	}

	@Test
	@DisplayName("getPresigner：懒加载单例复用")
	void getPresigner_lazyInitSingletonPerPlatform() {
		// 验证 Presigner 与 Client 独立缓存，且各自懒加载单例。
		S3ClientManager manager = buildConfiguredManager();

		S3Presigner first = manager.getPresigner(StoragePlatformEnum.MINIO);
		S3Presigner second = manager.getPresigner(StoragePlatformEnum.MINIO);

		assertThat(first).isSameAs(second);
		manager.shutdown();
	}

	@Test
	@DisplayName("shutdown：清空缓存后再次获取会重新构建")
	void shutdown_recreatesClientOnNextAccess() {
		// 验证停机方法完成缓存清理，避免关闭后依然引用已释放实例。
		S3ClientManager manager = buildConfiguredManager();
		S3Client before = manager.getClient(StoragePlatformEnum.MINIO);

		manager.shutdown();
		S3Client after = manager.getClient(StoragePlatformEnum.MINIO);

		assertThat(after).isNotSameAs(before);
		manager.shutdown();
	}

	private S3ClientManager buildConfiguredManager() {
		FileUploadProperties properties = new FileUploadProperties();
		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint(MINIO_ENDPOINT);
		profile.setRegion("us-east-1");
		profile.setAccessKey(ACCESS_KEY);
		profile.setSecretKey(SECRET_KEY);
		profile.setBucket(BUCKET);
		profile.setPathStyleAccess(true);
		properties.getPlatforms().put(StoragePlatformEnum.MINIO, profile);
		return new S3ClientManager(S3PlatformProfileResolverFixtures.defaultResolver(properties));
	}

}
