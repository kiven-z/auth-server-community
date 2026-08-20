package com.auth.service.system.file.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 文件上传域结果码
 *
 * @author Bunny
 */
@Getter
public enum FileUploadResultCode implements SystemResultCode {

	/**
	 * 存储平台不支持
	 */
	FILE_STORAGE_PLATFORM_UNSUPPORTED(400, 342, "FILE_STORAGE_PLATFORM_UNSUPPORTED",
			"system.file.storage.platform_unsupported"),

	/**
	 * 存储平台配置缺失
	 */
	FILE_STORAGE_CONFIG_MISSING(500, 343, "FILE_STORAGE_CONFIG_MISSING", "system.file.storage.config_missing"),

	/**
	 * 文件上传失败
	 */
	FILE_UPLOAD_FAILED(502, 344, "FILE_UPLOAD_FAILED", "system.file.upload.failed"),

	/**
	 * 存储平台重复注册
	 */
	FILE_STORAGE_PLATFORM_DUPLICATED(500, 345, "FILE_STORAGE_PLATFORM_DUPLICATED",
			"system.file.storage.platform_duplicated"),

	/**
	 * 文件记录不存在
	 */
	FILE_RECORD_NOT_FOUND(404, 346, "FILE_RECORD_NOT_FOUND", "system.file.record.not_found"),

	/**
	 * 文件类型不支持预览
	 */
	FILE_PREVIEW_CONTENT_TYPE_NOT_ALLOWED(422, 348, "FILE_PREVIEW_CONTENT_TYPE_NOT_ALLOWED",
			"system.file.preview.content_type_not_allowed"),

	/**
	 * 文件后缀在全局黑名单中
	 */
	FILE_EXTENSION_BLOCKED(400, 349, "FILE_EXTENSION_BLOCKED", "system.file.upload.extension_blocked"),

	/**
	 * 文件大小超过业务类型上限
	 */
	FILE_SIZE_EXCEEDED(400, 350, "FILE_SIZE_EXCEEDED", "system.file.upload.size_exceeded"),

	/**
	 * 文件真实类型不在业务白名单中
	 */
	FILE_TYPE_NOT_ALLOWED(400, 351, "FILE_TYPE_NOT_ALLOWED", "system.file.upload.type_not_allowed"),

	/**
	 * 无法检测文件内容类型
	 */
	FILE_CONTENT_DETECT_FAILED(400, 352, "FILE_CONTENT_DETECT_FAILED", "system.file.upload.content_detect_failed");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	FileUploadResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
