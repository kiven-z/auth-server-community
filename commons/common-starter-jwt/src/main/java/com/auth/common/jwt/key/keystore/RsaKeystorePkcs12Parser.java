package com.auth.common.jwt.key.keystore;

import com.auth.common.jwt.exception.JwtKeyLoadException;
import com.auth.common.jwt.key.RsaKeyPairMaterial;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * PKCS12 解析为 RSA 密钥对
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeystorePkcs12Parser {

	/**
	 * 解析 PKCS12
	 * @param input 输入流
	 * @param password 密码
	 * @param alias 别名
	 * @return 密钥对
	 * @throws KeyStoreException 异常
	 */
	public static RsaKeyPairMaterial parse(InputStream input, String password, String alias) throws KeyStoreException,
			CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
		// 获取 KeyStore 实例
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		// 加载 KeyStore
		keyStore.load(input, password.toCharArray());
		// 获取 Key
		Key key = keyStore.getKey(alias, password.toCharArray());
		// 如果 Key 不是 PrivateKey，则抛出 JwtKeyLoadException
		if (!(key instanceof PrivateKey privateKey)) {
			throw new JwtKeyLoadException("Private key missing for alias: " + alias);
		}

		// 获取 Certificate
		Certificate certificate = keyStore.getCertificate(alias);
		// 如果 Certificate 为空，则抛出 JwtKeyLoadException
		if (certificate == null) {
			throw new JwtKeyLoadException("Certificate missing for alias: " + alias);
		}

		// 获取 PublicKey
		PublicKey publicKey = certificate.getPublicKey();
		return new RsaKeyPairMaterial(privateKey, publicKey);
	}

}
