package com.auth.service.system.file.storage.core;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 存储桶启动初始化器
 *
 * @author Bunny
 */
@Slf4j
@Component
public class StorageBucketBootstrapRunner {

	private final FileUploadProperties properties;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	public StorageBucketBootstrapRunner(FileUploadProperties properties, StoragePlatformFacadeRegistry facadeRegistry) {
		this.properties = properties;
		this.facadeRegistry = facadeRegistry;
	}

	/**
	 * 应用就绪后执行已配置平台的存储桶初始化
	 * @param event 应用就绪事件
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(ApplicationReadyEvent event) {
		StoragePlatformEnum platform = properties.getDefaultPlatform();
		if (platform == null) {
			log.info("Skip storage bucket initialization due to missing default platform config");
			return;
		}

		StoragePlatformFacade facade = facadeRegistry.resolve(platform);
		if (facade == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED, platform.name());
		}
		if (!facade.validator().isConfigured()) {
			log.info("Skip storage bucket initialization due to incomplete config: platform={}", platform);
			return;
		}
		facade.initializer().ensureBucketReady();
		log.info("Storage bucket initialization completed: platform={}", platform);
	}

}
