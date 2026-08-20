package com.auth.common.jwt.key;

import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.jwt.exception.JwtKeyLoadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static com.auth.common.jwt.model.SignatureAlgorithm.RS256;
import static org.junit.jupiter.api.Assertions.*;

class RsaKeyStoreLoaderTest {

	/**
	 * 创建 RSA 基础属性
	 * @return 属性
	 */
	private static JwtProperties rsaBaseProps() {
		JwtProperties props = new JwtProperties();
		props.setAlgorithm(RS256);
		props.setIssuer("rsa-loader-test");
		props.setAccessExpired(3600);
		props.setRefreshExpired(7200);
		props.setAlias("jwtkey");
		return props;
	}

	@Test
	@DisplayName("拒绝空密钥")
	void missingKeystoreWithoutAutoGenerateThrows(@TempDir Path tempDir) {
		Path missing = tempDir.resolve("absent.p12");
		JwtProperties props = rsaBaseProps();
		props.setKeystorePath(missing.toAbsolutePath().toString());
		props.setPassword("any-password");
		props.setAutoGenerate(false);
		props.validate();
		assertThrows(JwtKeyLoadException.class, () -> RsaKeyStoreLoader.load(props));
	}

	@Test
	@DisplayName("自动生成密钥库")
	void autoGenerateCreatesAndLoadsKeystore(@TempDir Path tempDir) {
		Path target = tempDir.resolve("auto-gen.p12");
		JwtProperties props = rsaBaseProps();
		props.setKeystorePath(target.toAbsolutePath().toString());
		props.setPassword("correct-password");
		props.setAutoGenerate(true);
		props.validate();
		RsaKeyPairMaterial first = assertDoesNotThrow(() -> RsaKeyStoreLoader.load(props));
		assertNotNull(first.privateKey());
		assertNotNull(first.publicKey());
		RsaKeyPairMaterial second = assertDoesNotThrow(() -> RsaKeyStoreLoader.load(props));
		assertNotNull(second.privateKey());
	}

	@Test
	@DisplayName("拒绝错误密码")
	void wrongPasswordThrows(@TempDir Path tempDir) {
		Path target = tempDir.resolve("pwd-test.p12");
		JwtProperties good = rsaBaseProps();
		good.setKeystorePath(target.toAbsolutePath().toString());
		good.setAutoGenerate(true);
		good.setPassword("correct-password");
		good.setAlias("jwtkey");
		good.validate();
		assertDoesNotThrow(() -> RsaKeyStoreLoader.load(good));

		JwtProperties bad = rsaBaseProps();
		bad.setKeystorePath(target.toAbsolutePath().toString());
		bad.setAutoGenerate(false);
		bad.setPassword("wrong-password");
		bad.setAlias("jwtkey");
		bad.validate();
		assertThrows(JwtKeyLoadException.class, () -> RsaKeyStoreLoader.load(bad));
	}

}
