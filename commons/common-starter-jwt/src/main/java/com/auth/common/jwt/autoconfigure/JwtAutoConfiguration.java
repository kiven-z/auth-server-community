package com.auth.common.jwt.autoconfigure;

import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.key.HmacSecretKeys;
import com.auth.common.jwt.key.RsaKeyPairMaterial;
import com.auth.common.jwt.key.RsaKeyStoreLoader;
import com.auth.common.jwt.provider.HmacJwtTokenProvider;
import com.auth.common.jwt.provider.RsaJwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.crypto.SecretKey;

/**
 * JWT 自动配置：按 auth.common.jwt.algorithm 选择 HS256 / RS256
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) {
		log.info("Common JWT starter initialized successfully");
	}

	/**
	 * HS256 自动配置
	 */
	@ConditionalOnProperty(prefix = "auth.common.jwt", name = "algorithm", havingValue = "HS256", matchIfMissing = true)
	@Primary
	@Configuration(proxyBeanMethods = false)
	static class JwtHmacAutoConfiguration {

		@ConditionalOnMissingBean(JwtTokenProvider.class)
		@Bean
		@Primary
		JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
			SecretKey secretKey = HmacSecretKeys.fromUtf8Secret(jwtProperties.getSecret());
			return new HmacJwtTokenProvider(jwtProperties, secretKey);
		}

	}

	/**
	 * RS256 自动配置
	 */
	@ConditionalOnProperty(prefix = "auth.common.jwt", name = "algorithm", havingValue = "RS256")
	@Configuration(proxyBeanMethods = false)
	static class JwtRsaAutoConfiguration {

		@ConditionalOnMissingBean(JwtTokenProvider.class)
		@Bean
		JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
			RsaKeyPairMaterial material = RsaKeyStoreLoader.load(jwtProperties);
			return new RsaJwtTokenProvider(jwtProperties, material.privateKey(), material.publicKey());
		}

	}

}
