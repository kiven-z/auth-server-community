package com.auth.service.system.file.storage.core.s3;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;

/**
 * S3 协议配置来源：按平台从 legacy 配置块适配出 {@link S3PlatformProfile}
 *
 * @author Bunny
 */
public interface S3PlatformProfileSource {

	/**
	 * 当前来源所服务的存储平台
	 * @return 存储平台
	 */
	StoragePlatformEnum platform();

	/**
	 * 从 legacy 配置块解析 S3 协议配置
	 * @param properties 文件上传配置
	 * @return S3 协议配置
	 */
	S3PlatformProfile resolveFallback(FileUploadProperties properties);

}
