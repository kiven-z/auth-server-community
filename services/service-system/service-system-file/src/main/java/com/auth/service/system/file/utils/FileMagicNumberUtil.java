package com.auth.service.system.file.utils;

import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import lombok.experimental.UtilityClass;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * 基于 Apache Tika 的文件魔数 / MIME 检测。
 *
 * @author Bunny
 */
@UtilityClass
public class FileMagicNumberUtil {

	private static final Tika TIKA = new Tika();

	/**
	 * 检测上传文件的真实 MIME 类型
	 * @param file 上传文件
	 * @return 检测到的 MIME（小写主类型，不含参数）
	 */
	public static String detectMimeType(MultipartFile file) {
		if (file == null) {
			throw new FileStorageException(FileUploadResultCode.FILE_CONTENT_DETECT_FAILED, "null");
		}
		String filename = file.getOriginalFilename();
		try (InputStream inputStream = file.getInputStream()) {
			return normalizeMimeType(TIKA.detect(inputStream, filename));
		}
		catch (IOException exception) {
			throw new FileStorageException(FileUploadResultCode.FILE_CONTENT_DETECT_FAILED, exception, filename);
		}
	}

	/**
	 * 规范化 MIME：去空白、小写、去掉 charset 等参数
	 * @param mimeType 原始 MIME
	 * @return 规范化结果；空输入返回空字符串
	 */
	public static String normalizeMimeType(String mimeType) {
		if (mimeType == null || mimeType.isBlank()) {
			return "";
		}
		String normalized = mimeType.trim().toLowerCase(Locale.ROOT);
		int separatorIndex = normalized.indexOf(';');
		if (separatorIndex >= 0) {
			normalized = normalized.substring(0, separatorIndex).trim();
		}
		return normalized;
	}

}
