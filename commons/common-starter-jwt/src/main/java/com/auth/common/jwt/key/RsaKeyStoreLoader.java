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
 * 编排 classpath / 文件系统加载与可选自动生成 编排 RSA 密钥库加载流程：解析路径、自动生成（如果配置）、打开流、解析 PKCS12
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeyStoreLoader {

	/**
	 * 加载 RSA 密钥对
	 * @param properties 配置
	 * @return 密钥对
	 * @throws JwtKeyLoadException 异常
	 */
	public static RsaKeyPairMaterial load(JwtProperties properties) {
		// 如果别名为空，则抛出 JwtKeyLoadException
		String alias = properties.getAlias();
		if (CharSequenceUtil.isBlank(alias)) {
			throw new JwtKeyLoadException("Keystore alias must not be blank.");
		}

		// 获取 keystore 路径
		String location = properties.getKeystorePath();
		// 如果路径以 classpath: 开头，则设置 classpath 为 true
		boolean classpath = location.startsWith("classpath:");
		// 如果路径不是 classpath:，则解析文件系统路径
		Path filesystemPath = classpath ? null : RsaKeystorePathResolver.resolveFilesystem(location);
		// 如果路径不是 classpath:，则确保生成 PKCS12
		if (!classpath) {
			RsaKeystoreAutoBootstrap.ensureGeneratedIfConfigured(properties, filesystemPath);
		}

		// 打开输入流
		try (InputStream in = openStream(location, classpath, filesystemPath)) {
			// 解析 PKCS12
			return RsaKeystorePkcs12Parser.parse(in, properties.getPassword(), alias);
		}
		catch (JwtKeyLoadException ex) {
			// 如果抛出 JwtKeyLoadException，则抛出
			throw ex;
		}
		catch (Exception ex) {
			throw new JwtKeyLoadException("Failed to load RSA keystore: " + location, ex);
		}
	}

	/**
	 * 打开输入流
	 * @param location 路径
	 * @param classpath 是否 classpath
	 * @param filesystemPath 文件系统路径
	 * @return 输入流
	 * @throws IOException 异常
	 */
	private static InputStream openStream(String location, boolean classpath, Path filesystemPath) throws IOException {
		// 如果 classpath 为 true，则打开 classpath 输入流
		if (classpath) {
			return RsaKeystoreClasspathStreamSource.openIfPresent(location);
		}

		// 否则打开文件系统输入流
		return RsaKeystoreFilesystemStreamSource.openIfPresent(filesystemPath);
	}

}
