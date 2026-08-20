package com.auth.service.system.file.storage.platform.aliyun;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.config.properties.platform.AliyunOssStorageProperties;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileSource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 阿里云 OSS 平台 S3 协议配置来源
 *
 * @author Bunny
 */
@Component
public class AliyunOssS3PlatformProfileSource implements S3PlatformProfileSource {

	/**
	 * 阿里云 OSS 走 S3 兼容协议时的兜底 region（S3 SDK 强制要求 region 非空）
	 */
	private static final String ALIYUN_S3_FALLBACK_REGION = "oss-cn-hangzhou";

	/**
	 * 从阿里云 endpoint 推断 region 名，例如 oss-cn-hangzhou.aliyuncs.com → oss-cn-hangzhou
	 * @param endpoint 阿里云 endpoint
	 * @return 推断出的 region 名，无法解析时兜底为默认值
	 */
	private static String deriveAliyunRegion(String endpoint) {
		if (CharSequenceUtil.isBlank(endpoint)) {
			return ALIYUN_S3_FALLBACK_REGION;
		}
		String host = CharSequenceUtil.removePrefixIgnoreCase(endpoint, "https://");
		host = CharSequenceUtil.removePrefixIgnoreCase(host, "http://");
		int dotIndex = host.indexOf('.');
		if (dotIndex <= 0) {
			return ALIYUN_S3_FALLBACK_REGION;
		}
		return host.substring(0, dotIndex);
	}

	@Override
	public StoragePlatformEnum platform() {
		return StoragePlatformEnum.ALIYUN_OSS;
	}

	@Override
	public S3PlatformProfile resolveFallback(FileUploadProperties properties) {
		AliyunOssStorageProperties aliyunOss = Objects.requireNonNullElseGet(properties.getAliyunOss(),
				AliyunOssStorageProperties::new);

		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint(aliyunOss.getEndpoint());
		profile.setRegion(deriveAliyunRegion(aliyunOss.getEndpoint()));
		profile.setAccessKey(aliyunOss.getAccessKeyId());
		profile.setSecretKey(aliyunOss.getAccessKeySecret());
		profile.setBucket(aliyunOss.getBucket());
		profile.setPublicUrl(aliyunOss.getPublicUrl());
		// 阿里云 OSS 仅支持 virtual-hosted；path-style 会 403
		profile.setPathStyleAccess(false);
		return profile;
	}

}
