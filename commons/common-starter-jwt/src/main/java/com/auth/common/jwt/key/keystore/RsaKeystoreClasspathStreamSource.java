package com.auth.common.jwt.key.keystore;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * classpath: PKCS12 流打开（与 Spring Boot 资源解析一致） 从 classpath 资源打开 PKCS12 输入流
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeystoreClasspathStreamSource {

	/**
	 * 打开 PKCS12 流
	 * @param location 位置
	 * @return 流
	 * @throws IOException 异常
	 */
	public static InputStream openIfPresent(String location) throws IOException {
		Resource resource = new DefaultResourceLoader().getResource(location);
		if (!resource.exists()) {
			return null;
		}
		return resource.getInputStream();
	}

}
