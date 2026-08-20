package com.auth.service.system.file.model.value;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import lombok.Builder;
import lombok.Value;

/**
 * 对象存储上传结果
 *
 * @author Bunny
 */
@Value
@Builder
public class StoredFile {

	/**
	 * 存储平台
	 */
	StoragePlatformEnum storagePlatform;

	/**
	 * 存储桶名称
	 */
	String bucket;

	/**
	 * 对象键
	 */
	String objectKey;

	/**
	 * 访问URL
	 */
	String url;

	/**
	 * 原始文件名
	 */
	String originalName;

	/**
	 * 扩展名
	 */
	String extension;

	/**
	 * 内容类型
	 */
	String contentType;

	/**
	 * 文件大小
	 */
	Long size;

	/**
	 * 对象ETag
	 */
	String etag;

}
