package com.auth.common.jwt.key.keystore;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * 从 classpath 打开 PKCS12 输入流，解析方式与 Spring Boot 资源一致
 *
 * @author Bunny
 */
@UtilityClass
public class RsaKeystoreClasspathStreamSource {

	public static InputStream openIfPresent(String location) throws IOException {
		Resource resource = new DefaultResourceLoader().getResource(location);
		if (!resource.exists()) {
			return null;
		}
		return resource.getInputStream();
	}

}
