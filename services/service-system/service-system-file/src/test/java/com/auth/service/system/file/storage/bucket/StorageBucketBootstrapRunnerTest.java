package com.auth.service.system.file.storage.bucket;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.storage.core.StorageBucketBootstrapRunner;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.bucket.StorageBucketInitializer;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * {@link StorageBucketBootstrapRunner} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("StorageBucketBootstrapRunner 启动初始化")
@ExtendWith(MockitoExtension.class)
class StorageBucketBootstrapRunnerTest {

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private StorageBucketInitializer bucketInitializer;

	@Mock
	private StoragePlatformConfigValidator validator;

	@Test
	@DisplayName("onApplicationReady：平台已配置时执行初始化")
	void onApplicationReady_runsInitializerWhenPlatformConfigured() {
		// 验证默认平台配置完整时会触发对应初始化器。
		FileUploadProperties properties = new FileUploadProperties();
		properties.setDefaultPlatform(StoragePlatformEnum.MINIO);
		StorageBucketBootstrapRunner runner = new StorageBucketBootstrapRunner(properties, facadeRegistry);
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.validator()).thenReturn(validator);
		when(validator.isConfigured()).thenReturn(true);
		when(storagePlatformFacade.initializer()).thenReturn(bucketInitializer);

		runner.onApplicationReady(null);

		verify(facadeRegistry).resolve(StoragePlatformEnum.MINIO);
		verify(storagePlatformFacade).validator();
		verify(validator).isConfigured();
		verify(storagePlatformFacade).initializer();
		verify(bucketInitializer).ensureBucketReady();
	}

	@Test
	@DisplayName("onApplicationReady：平台未配置时跳过初始化")
	void onApplicationReady_skipsInitializerWhenPlatformNotConfigured() {
		// 验证默认平台配置缺失时不会触发初始化器避免无效失败。
		FileUploadProperties properties = new FileUploadProperties();
		properties.setDefaultPlatform(StoragePlatformEnum.MINIO);
		StorageBucketBootstrapRunner runner = new StorageBucketBootstrapRunner(properties, facadeRegistry);
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.validator()).thenReturn(validator);
		when(validator.isConfigured()).thenReturn(false);

		runner.onApplicationReady(null);

		verify(facadeRegistry).resolve(StoragePlatformEnum.MINIO);
		verify(storagePlatformFacade).validator();
		verify(validator).isConfigured();
		verify(storagePlatformFacade, never()).initializer();
		verifyNoInteractions(bucketInitializer);
	}

	@Test
	@DisplayName("onApplicationReady：默认平台为空时直接跳过")
	void onApplicationReady_skipsWhenDefaultPlatformMissing() {
		// 验证未配置默认平台时不会进入门面解析和初始化流程。
		FileUploadProperties properties = new FileUploadProperties();
		properties.setDefaultPlatform(null);
		StorageBucketBootstrapRunner runner = new StorageBucketBootstrapRunner(properties, facadeRegistry);

		runner.onApplicationReady(null);

		verifyNoInteractions(facadeRegistry, storagePlatformFacade, bucketInitializer, validator);
	}

}
