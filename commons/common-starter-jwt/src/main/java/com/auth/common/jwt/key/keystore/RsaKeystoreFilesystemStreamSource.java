package com.auth.common.jwt.key.keystore;

import cn.hutool.core.io.FileUtil;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件系统 PKCS12 流打开 从文件系统路径打开 PKCS12 输入流
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeystoreFilesystemStreamSource {

	/**
	 * 打开 PKCS12 流
	 * @param path 路径
	 * @return 流
	 * @throws IOException 异常
	 */
	public static InputStream openIfPresent(Path path) throws IOException {
		// 如果路径为空或文件不存在，则返回 null
		if (path == null || !FileUtil.exist(path.toFile())) {
			return null;
		}
		return Files.newInputStream(path);
	}

}
