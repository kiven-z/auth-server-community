package com.auth.common.jwt.key.keystore;

import lombok.experimental.UtilityClass;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将配置中的路径解析（支持 file:）
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeystorePathResolver {

	/**
	 * 解析文件系统路径
	 * @param location 位置
	 * @return 路径
	 */
	public static Path resolveFilesystem(String location) {
		// 如果位置以 file: 开头，则返回 URI 创建的路径
		String prefix = "file:";
		if (location.startsWith(prefix)) {
			return Paths.get(URI.create(location));
		}

		return Paths.get(location);
	}

}
