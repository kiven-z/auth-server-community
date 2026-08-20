package com.auth.common.jwt.key;

import com.auth.common.jwt.exception.JwtKeyLoadException;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.UtilityClass;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * HS256 密钥派生 从 UTF-8 字符串派生符合 HS256 要求的 SecretKey（长度 ≥ 32 字节）
 *
 * @author Bunny
 */
@UtilityClass
public class HmacSecretKeys {

	/**
	 * 从 UTF-8 密钥生成 SecretKey
	 * @param secret 密钥
	 * @return SecretKey
	 * @throws JwtKeyLoadException 异常
	 */
	public static SecretKey fromUtf8Secret(String secret) {
		if (secret == null || secret.isBlank()) {
			throw new JwtKeyLoadException("HMAC secret must not be blank.");
		}
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);

		// 如果字节长度小于 32，则抛出 JwtKeyLoadException
		boolean bytesLength = bytes.length < 32;
		if (bytesLength) {
			throw new JwtKeyLoadException("HMAC secret must be at least 256 bits (32 UTF-8 bytes) for HS256.");
		}

		return Keys.hmacShaKeyFor(bytes);
	}

}
