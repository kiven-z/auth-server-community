package com.auth.common.jwt.key;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.exception.JwtKeyLoadException;
import com.auth.common.jwt.key.keystore.*;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 编排 RSA 密钥库加载：解析路径、按配置自动生成、打开流并解析 PKCS12
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeyStoreLoader {

	/**
	 * 加载 RSA 密钥对
	 * @param properties JWT 配置
	 * @return 密钥对
	 */
	public static RsaKeyPairMaterial load(JwtProperties properties) {
		String alias = properties.getAlias();
		if (CharSequenceUtil.isBlank(alias)) {
			throw new JwtKeyLoadException("Keystore alias must not be blank.");
		}

		String location = properties.getKeystorePath();
		boolean classpath = location.startsWith("classpath:");
		Path filesystemPath = classpath ? null : RsaKeystorePathResolver.resolveFilesystem(location);
		if (!classpath) {
			RsaKeystoreAutoBootstrap.ensureGeneratedIfConfigured(properties, filesystemPath);
		}

		try (InputStream in = openStream(location, classpath, filesystemPath)) {
			return RsaKeystorePkcs12Parser.parse(in, properties.getPassword(), alias);
		}
		catch (JwtKeyLoadException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JwtKeyLoadException("Failed to load RSA keystore: " + location, ex);
		}
	}

	private static InputStream openStream(String location, boolean classpath, Path filesystemPath) throws IOException {
		if (classpath) {
			return RsaKeystoreClasspathStreamSource.openIfPresent(location);
		}

		return RsaKeystoreFilesystemStreamSource.openIfPresent(filesystemPath);
	}

}
