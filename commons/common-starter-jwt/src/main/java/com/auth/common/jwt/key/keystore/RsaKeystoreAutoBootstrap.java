package com.auth.common.jwt.key.keystore;

import cn.hutool.core.io.FileUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.exception.JwtKeyLoadException;
import com.auth.common.jwt.util.KeyStoreGeneratorUtil;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Path;

/**
 * 开发联调：在目标文件不存在时生成 PKCS12 开发联调时，如果 autoGenerate=true 且 keystore 文件不存在，则自动生成新的 PKCS12
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeystoreAutoBootstrap {

	/**
	 * 确保生成 PKCS12
	 * @param properties 配置
	 * @param filesystemPath 文件路径
	 */
	public static void ensureGeneratedIfConfigured(JwtProperties properties, Path filesystemPath) {
		// 如果文件路径为空或自动生成为 false，则返回
		if (filesystemPath == null || !properties.isAutoGenerate()) {
			return;
		}

		// 如果文件存在，则返回
		File filesystemPathFile = filesystemPath.toFile();
		if (FileUtil.exist(filesystemPathFile)) {
			return;
		}

		// 生成 PKCS12
		try {
			Path parent = filesystemPath.getParent();
			if (parent != null) {
				File file = parent.toFile();
				FileUtil.mkdir(file);
			}

			String password = properties.getPassword();
			KeyStoreGeneratorUtil.generateToFile(filesystemPath.toString(), password, properties.getAlias());
		}
		catch (Exception ex) {
			throw new JwtKeyLoadException("Failed to auto-generate keystore at " + filesystemPath, ex);
		}
	}

}
