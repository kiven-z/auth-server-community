package com.auth.service.system.file.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件临时访问地址签名器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileUrlSigner {

	/**
	 * MIME 类型通配后缀，如 image/*
	 */
	private static final String CONTENT_TYPE_WILDCARD_SUFFIX = "/*";

	/**
	 * 通配后缀中的星号，用于截取类型前缀
	 */
	private static final String CONTENT_TYPE_WILDCARD_CHAR = "*";

	private final FileUploadProperties fileUploadProperties;

	private final StoragePlatformFacadeRegistry facadeRegistry;

	/**
	 * 生成带过期时间的访问地址
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @param contentType 文件类型
	 * @param platform 存储平台
	 * @return 临时访问地址
	 */
	public String sign(String bucket, String objectKey, String contentType, StoragePlatformEnum platform) {
		requireSignArgs(bucket, objectKey, platform);
		if (!isPreviewContentTypeAllowed(contentType)) {
			throw new FileStorageException(FileUploadResultCode.FILE_PREVIEW_CONTENT_TYPE_NOT_ALLOWED, contentType);
		}

		// 按平台生成预签名访问地址
		FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
		return provider.presignGetUrl(bucket, objectKey, fileUploadProperties.getExpireSeconds());
	}

	/**
	 * 生成下载访问地址（不做预览 MIME 白名单校验）
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @param platform 存储平台
	 * @return 临时下载地址
	 */
	public String signDownload(String bucket, String objectKey, StoragePlatformEnum platform) {
		requireSignArgs(bucket, objectKey, platform);

		// 按平台生成预签名访问地址
		FileStorageProvider provider = facadeRegistry.resolve(platform).provider();
		return provider.presignGetUrl(bucket, objectKey, fileUploadProperties.getExpireSeconds());
	}

	/**
	 * 判断文件类型是否允许预览
	 * @param contentType 文件类型
	 * @return 允许预览时返回 true；白名单为空时视为全部允许
	 */
	public boolean isPreviewContentTypeAllowed(String contentType) {
		List<String> allowedTypes = fileUploadProperties.getAllowedContentTypes();
		if (CollUtil.isEmpty(allowedTypes)) {
			return true;
		}
		if (CharSequenceUtil.isBlank(contentType)) {
			return false;
		}

		return allowedTypes.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(CharSequenceUtil::trim)
			.anyMatch(pattern -> matchesContentType(contentType, pattern));
	}

	/**
	 * 校验签名所需参数
	 * @param bucket 存储桶
	 * @param objectKey 对象键
	 * @param platform 存储平台
	 */
	private void requireSignArgs(String bucket, String objectKey, StoragePlatformEnum platform) {
		if (platform == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_PLATFORM_UNSUPPORTED, "null");
		}
		if (CharSequenceUtil.isBlank(bucket)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "bucket");
		}
		if (CharSequenceUtil.isBlank(objectKey)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, "objectKey");
		}
	}

	/**
	 * 内容类型是否匹配白名单规则（支持 image/*）
	 * @param contentType 实际类型
	 * @param pattern 白名单规则
	 * @return 是否匹配
	 */
	private boolean matchesContentType(String contentType, String pattern) {
		if (CharSequenceUtil.isBlank(pattern)) {
			return false;
		}
		if (CharSequenceUtil.endWith(pattern, CONTENT_TYPE_WILDCARD_SUFFIX)) {
			String prefix = CharSequenceUtil.removeSuffix(pattern, CONTENT_TYPE_WILDCARD_CHAR);
			return CharSequenceUtil.startWithIgnoreCase(contentType, prefix);
		}
		return CharSequenceUtil.equalsIgnoreCase(contentType, pattern);
	}

}
