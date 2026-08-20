package com.auth.common.jwt.util;

import com.auth.common.jwt.exception.JwtKeyLoadException;
import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * 生成 PKCS12（RSA 2048）用于 RS256 开发联调
 *
 * @author Bunny
 */
@UtilityClass
public class KeyStoreGeneratorUtil {

	private static final int RSA_BITS = 2048;

	private static final int DEFAULT_VALIDITY_DAYS = 3650;

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	/**
	 * 生成到文件
	 * @param path 路径
	 * @param password 密码
	 * @param alias 别名
	 * @throws JwtKeyLoadException 异常
	 */
	public static void generateToFile(String path, String password, String alias) {
		try {
			// 生成 KeyPair
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
			kpg.initialize(RSA_BITS, new SecureRandom());
			KeyPair keyPair = kpg.generateKeyPair();

			// 生成证书
			long now = System.currentTimeMillis();
			Date notBefore = new Date(now);
			Date notAfter = new Date(now + DEFAULT_VALIDITY_DAYS * 86_400_000L);
			X500Name dn = new X500Name("CN=AuthAdmin");
			BigInteger serial = BigInteger.valueOf(now);
			JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dn, serial, notBefore, notAfter,
					dn, keyPair.getPublic());
			ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
				.setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.build(keyPair.getPrivate());
			X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.getCertificate(certBuilder.build(signer));

			// 生成 KeyStore
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			keyStore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(),
					new java.security.cert.Certificate[] { cert });

			try (FileOutputStream fos = new FileOutputStream(path)) {
				keyStore.store(fos, password.toCharArray());
			}
		}
		catch (Exception ex) {
			throw new JwtKeyLoadException("Failed to generate PKCS12 keystore at " + path, ex);
		}
	}

}
