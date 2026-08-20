package com.auth.service.system.file.support;

import com.auth.module.file.api.policy.FileBizType;
import com.auth.module.file.api.policy.FileExtensionBlacklist;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.utils.FileMagicNumberUtil;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传内容策略校验：后缀黑名单、大小上限、魔数 MIME 白名单。
 *
 * @author Bunny
 */
@UtilityClass
public class FileUploadContentValidator {

	/**
	 * 按业务类型校验上传文件
	 * @param file 上传文件（调用方已保证非空）
	 * @param bizType 业务类型
	 */
	public static void validate(MultipartFile file, FileBizType bizType) {
		String originalFilename = file.getOriginalFilename();
		rejectBlockedExtension(originalFilename);
		rejectOversized(file, bizType);
		rejectDisallowedMime(file, bizType);
	}

	/**
	 * 拒绝命中全局后缀黑名单的文件名
	 * @param originalFilename 原始文件名
	 */
	private static void rejectBlockedExtension(String originalFilename) {
		if (!FileExtensionBlacklist.isBlocked(originalFilename)) {
			return;
		}
		String extension = FileExtensionBlacklist.extractExtension(originalFilename);
		throw new FileStorageException(FileUploadResultCode.FILE_EXTENSION_BLOCKED, extension);
	}

	/**
	 * 拒绝超过业务类型大小上限的文件
	 * @param file 上传文件
	 * @param bizType 业务类型
	 */
	private static void rejectOversized(MultipartFile file, FileBizType bizType) {
		long size = file.getSize();
		if (size <= bizType.maxSizeBytes()) {
			return;
		}
		throw new FileStorageException(FileUploadResultCode.FILE_SIZE_EXCEEDED, bizType.getMaxSizeMb(),
				bizType.getCode());
	}

	/**
	 * 拒绝魔数 MIME 不在业务白名单内的文件
	 * @param file 上传文件
	 * @param bizType 业务类型
	 */
	private static void rejectDisallowedMime(MultipartFile file, FileBizType bizType) {
		String detectedMime = FileMagicNumberUtil.detectMimeType(file);
		if (bizType.allowsMimeType(detectedMime)) {
			return;
		}
		throw new FileStorageException(FileUploadResultCode.FILE_TYPE_NOT_ALLOWED, detectedMime, bizType.getCode());
	}

}
