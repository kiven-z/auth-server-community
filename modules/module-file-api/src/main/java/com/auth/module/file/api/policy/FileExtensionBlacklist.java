package com.auth.module.file.api.policy;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.Set;

/**
 * 全局危险后缀黑名单（上传文件名末级后缀）。
 *
 * <p>
 * 仅作第一道拦截；真实类型仍须结合魔数与业务 MIME 白名单校验。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class FileExtensionBlacklist {

	/**
	 * 禁止上传的后缀（小写、不含点）
	 */
	private static final Set<String> BLOCKED = Set.of(
			// Windows 可执行 / 脚本
			"exe", "bat", "cmd", "com", "scr", "pif", "msi", "msp", "dll", "sys", "drv", "cpl", "hta", "reg",
			// Shell / PowerShell
			"sh", "bash", "zsh", "csh", "ksh", "ps1", "psd1", "psm1",
			// 脚本与宏
			"vbs", "vbe", "js", "jse", "wsf", "wsh", "php", "jsp", "asp", "aspx", "cgi",
			// 可执行包
			"jar", "war", "ear", "apk", "deb", "rpm");

	/**
	 * 判断原始文件名是否命中危险后缀
	 * @param originalFilename 原始文件名
	 * @return 命中黑名单则为 true
	 */
	public static boolean isBlocked(String originalFilename) {
		String extension = extractExtension(originalFilename);
		return extension != null && BLOCKED.contains(extension);
	}

	/**
	 * 提取文件名末级后缀（小写、不含点）；无后缀时返回 null
	 * @param originalFilename 原始文件名
	 * @return 后缀或 null
	 */
	public static String extractExtension(String originalFilename) {
		if (originalFilename == null || originalFilename.isBlank()) {
			return null;
		}
		String filename = originalFilename.trim();
		int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
		if (slash >= 0) {
			filename = filename.substring(slash + 1);
		}
		int dot = filename.lastIndexOf('.');
		if (dot < 0 || dot == filename.length() - 1) {
			return null;
		}
		return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

}
